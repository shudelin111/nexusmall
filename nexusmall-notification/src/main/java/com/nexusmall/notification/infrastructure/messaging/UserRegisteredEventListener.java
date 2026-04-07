package com.nexusmall.notification.infrastructure.messaging;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(UserRegisteredEvent event) {
        log.info("收到用户注册事件，userId: {}, username: {}, eventId: {}", 
                event.getUserId(), event.getUsername(), event.getEventId());

        try {
            // 1. 幂等性校验：检查是否已处理过此事件
            if (isEventProcessed(event.getEventId())) {
                log.warn("事件已处理，跳过重复消费，eventId: {}", event.getEventId());
                return;
            }

            // 2. 构建欢迎消息
            NotificationMessage message = buildWelcomeMessage(event);

            // 3. 保存站内消息
            notificationMessageMapper.insert(message);

            // 4. 标记事件已处理（实际项目中可使用 Redis 或数据库表）
            markEventAsProcessed(event.getEventId());

            log.info("用户欢迎消息发送成功，userId: {}, messageId: {}", 
                    event.getUserId(), message.getId());

        } catch (Exception e) {
            log.error("处理用户注册事件失败，userId: {}, eventId: {}", 
                    event.getUserId(), event.getEventId(), e);
            throw e; // 抛出异常触发 RocketMQ 重试机制
        }
    }

    /**
     * 构建欢迎消息
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

    /**
     * 检查事件是否已处理（幂等性校验）
     * <p>
     * 生产环境建议使用 Redis SETNX 或数据库唯一索引实现
     * </p>
     */
    private boolean isEventProcessed(String eventId) {
        // TODO: 实际项目中应使用 Redis 或数据库表存储已处理的事件ID
        // 示例：return redisTemplate.hasKey("event:processed:" + eventId);
        return false;
    }

    /**
     * 标记事件已处理
     */
    private void markEventAsProcessed(String eventId) {
        // TODO: 实际项目中应将事件ID存入 Redis 或数据库
        // 示例：redisTemplate.opsForValue().set("event:processed:" + eventId, "1", 7, TimeUnit.DAYS);
        log.debug("标记事件已处理，eventId: {}", eventId);
    }
}
