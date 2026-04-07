package com.nexusmall.notification.infrastructure.messaging;

import com.nexusmall.notification.application.service.IdempotencyService;
import com.nexusmall.notification.domain.entity.NotificationMessage;
import com.nexusmall.notification.domain.event.UserRegisteredEvent;
import com.nexusmall.notification.infrastructure.persistence.mapper.NotificationMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户注册事件监听器
 * <p>
 * 业界标准（Event-Driven Architecture + DDD）：
 * - 监听 Auth 服务发送的用户注册事件
 * - 自动为新用户发送欢迎消息（站内信）
 * - 实现最终一致性，提升系统吞吐量
 * - 支持幂等性校验，防止重复消费
 * - 符合 CQRS 模式的命令查询职责分离
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "USER_REGISTERED_TOPIC",
    consumerGroup = "notification-service-user-registered-consumer-group",
    consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
    messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
public class UserRegisteredEventListener implements RocketMQListener<UserRegisteredEvent> {

    private final NotificationMessageMapper notificationMessageMapper;
    private final IdempotencyService idempotencyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(UserRegisteredEvent event) {
        log.info("收到用户注册事件，userId: {}, username: {}, eventId: {}", 
                event.getUserId(), event.getUsername(), event.getEventId());

        try {
            // 1. 幂等性校验：使用 Redis SETNX 原子操作（过期时间7天）
            // 防止 RocketMQ 重试或网络抖动导致的重复消费
            if (!idempotencyService.tryProcess(event.getEventId(), 7 * 24 * 3600)) {
                log.warn("事件已处理，跳过重复消费，eventId: {}", event.getEventId());
                return; // 直接返回，不抛异常，避免不必要的重试
            }

            // 2. 构建欢迎消息
            // 根据用户信息生成个性化的欢迎内容
            NotificationMessage message = buildWelcomeMessage(event);

            // 3. 保存站内消息到数据库
            // 使用 MyBatis-Plus 的 insert 方法，自动填充 createTime 和 updateTime
            notificationMessageMapper.insert(message);

            log.info("用户欢迎消息发送成功，userId: {}, messageId: {}", 
                    event.getUserId(), message.getId());

        } catch (Exception e) {
            // 记录完整堆栈信息，便于问题排查
            log.error("处理用户注册事件失败，userId: {}, eventId: {}", 
                    event.getUserId(), event.getEventId(), e);
            // 抛出异常触发 RocketMQ 重试机制（最多重试16次）
            throw e;
        }
    }

    /**
     * 构建欢迎消息
     * <p>
     * 根据用户信息生成个性化的欢迎内容，包括：
     * - 消息标题：统一的欢迎语
     * - 消息内容：包含用户名的个性化欢迎词 + 平台功能介绍
     * - 消息类型：系统通知（type=1）
     * - 初始状态：未读（status=0）
     * </p>
     *
     * @param event 用户注册事件
     * @return 欢迎消息实体
     */
    private NotificationMessage buildWelcomeMessage(UserRegisteredEvent event) {
        NotificationMessage message = new NotificationMessage();
        message.setMemberId(event.getUserId());
        message.setTitle("欢迎加入 NexusMall");
        message.setContent(String.format(
            "亲爱的 %s，欢迎您加入 NexusMall！\n\n" +
            "我们为您提供优质的购物体验，包括：\n" +
            "• 海量商品，品质保证\n" +
            "• 限时秒杀，超值优惠\n" +
            "• 会员专享，积分兑换\n\n" +
            "祝您购物愉快！",
            event.getUsername()
        ));
        message.setType(1); // 1=系统通知
        message.setStatus(0); // 0=未读
        message.setBusinessType("USER");
        message.setBusinessId(event.getUserId());
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return message;
    }
}
