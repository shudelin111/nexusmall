package com.nexusmall.notification.infrastructure.messaging;

import com.nexusmall.notification.application.service.IdempotencyService;
import com.nexusmall.notification.domain.entity.NotificationMessage;
import com.nexusmall.notification.domain.event.FlashSaleStartedEvent;
import com.nexusmall.notification.infrastructure.persistence.mapper.NotificationMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 秒杀活动开始事件监听器
 * <p>
 * 业界标准（Event-Driven Architecture + Pub/Sub）：
 * - 监听 Promotion 服务发送的秒杀活动开始事件
 * - 批量向订阅用户推送秒杀提醒消息
 * - 支持高并发场景下的异步通知
 * - 实现最终一致性，提升系统响应速度
 * - 符合发布-订阅（Pub/Sub）模式
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "FLASH_SALE_STARTED_TOPIC",
    consumerGroup = "notification-service-flash-sale-consumer-group",
    consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
    messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
public class FlashSaleStartedEventListener implements RocketMQListener<FlashSaleStartedEvent> {

    private final NotificationMessageMapper notificationMessageMapper;
    private final IdempotencyService idempotencyService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(FlashSaleStartedEvent event) {
        log.info("收到秒杀活动开始事件，flashSaleId: {}, activityName: {}, eventId: {}", 
                event.getFlashSaleId(), event.getActivityName(), event.getEventId());

        try {
            // 1. 幂等性校验：使用 Redis SETNX 原子操作（过期时间7天）
            if (!idempotencyService.tryProcess(event.getEventId(), 7 * 24 * 3600)) {
                log.warn("事件已处理，跳过重复消费，eventId: {}", event.getEventId());
                return;
            }

            // 2. TODO: 查询订阅了该活动的用户列表
            // List<Long> subscribedUserIds = getSubscribedUsers(event.getFlashSaleId());

            // 3. 临时方案：发送全员通知（实际应按订阅关系推送）
            NotificationMessage message = buildFlashSaleNotification(event);
            notificationMessageMapper.insert(message);

            log.info("秒杀活动通知发送成功，flashSaleId: {}, messageId: {}", 
                    event.getFlashSaleId(), message.getId());

        } catch (Exception e) {
            log.error("处理秒杀活动开始事件失败，flashSaleId: {}, eventId: {}", 
                    event.getFlashSaleId(), event.getEventId(), e);
            throw e; // 抛出异常触发 RocketMQ 重试机制
        }
    }

    /**
     * 构建秒杀活动通知
     */
    private NotificationMessage buildFlashSaleNotification(FlashSaleStartedEvent event) {
        String startTimeStr = event.getStartTime() != null 
            ? event.getStartTime().format(FORMATTER) 
            : "未知";
        String endTimeStr = event.getEndTime() != null 
            ? event.getEndTime().format(FORMATTER) 
            : "未知";

        NotificationMessage message = new NotificationMessage();
        message.setMemberId(0L); // 0 表示全员通知
        message.setTitle("🔥 秒杀活动开始啦！");
        message.setContent(String.format(
            "【%s】秒杀活动火热进行中！\n\n" +
            "活动时间：%s - %s\n" +
            "折扣力度：%.1f 折\n" +
            "参与商品：%d 件\n\n" +
            "手慢无，赶紧抢购吧！",
            event.getActivityName(),
            startTimeStr,
            endTimeStr,
            event.getDiscountRate(),
            event.getProductIds() != null ? event.getProductIds().size() : 0
        ));
        message.setType(3); // 3=营销活动
        message.setStatus(0); // 0=未读
        message.setBusinessType("PROMOTION");
        message.setBusinessId(event.getFlashSaleId());
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return message;
    }
}
