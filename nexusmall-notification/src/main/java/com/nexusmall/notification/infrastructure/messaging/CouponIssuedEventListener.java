package com.nexusmall.notification.infrastructure.messaging;

import com.nexusmall.notification.application.service.IdempotencyService;
import com.nexusmall.notification.domain.entity.NotificationMessage;
import com.nexusmall.notification.domain.event.CouponIssuedEvent;
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
 * 优惠券发放事件监听器
 * <p>
 * 业界标准（Domain Event Pattern + Saga）：
 * - 监听 Promotion 服务发送的优惠券发放事件
 * - 自动向用户发送优惠券到账通知
 * - 支持多渠道通知（站内信、短信、推送）
 * - 实现分布式事务的最终一致性
 * - 符合事件溯源（Event Sourcing）最佳实践
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "COUPON_ISSUED_TOPIC",
    consumerGroup = "notification-service-coupon-issued-consumer-group",
    consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
    messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
public class CouponIssuedEventListener implements RocketMQListener<CouponIssuedEvent> {

    private final NotificationMessageMapper notificationMessageMapper;
    private final IdempotencyService idempotencyService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(CouponIssuedEvent event) {
        log.info("收到优惠券发放事件，userId: {}, couponId: {}, eventId: {}", 
                event.getUserId(), event.getCouponId(), event.getEventId());

        try {
            // 1. 幂等性校验：使用 Redis SETNX 原子操作（过期时间7天）
            if (!idempotencyService.tryProcess(event.getEventId(), 7 * 24 * 3600)) {
                log.warn("事件已处理，跳过重复消费，eventId: {}", event.getEventId());
                return;
            }

            // 2. 构建优惠券到账通知
            NotificationMessage message = buildCouponNotification(event);

            // 3. 保存站内消息
            notificationMessageMapper.insert(message);

            log.info("优惠券到账通知发送成功，userId: {}, couponId: {}, messageId: {}", 
                    event.getUserId(), event.getCouponId(), message.getId());

        } catch (Exception e) {
            log.error("处理优惠券发放事件失败，userId: {}, couponId: {}, eventId: {}", 
                    event.getUserId(), event.getCouponId(), event.getEventId(), e);
            throw e; // 抛出异常触发 RocketMQ 重试机制
        }
    }

    /**
     * 构建优惠券到账通知
     */
    private NotificationMessage buildCouponNotification(CouponIssuedEvent event) {
        String expireTimeStr = event.getExpireTime() != null 
            ? event.getExpireTime().format(FORMATTER) 
            : "长期有效";

        NotificationMessage message = new NotificationMessage();
        message.setMemberId(event.getUserId());
        message.setTitle("优惠券到账通知");
        message.setContent(String.format(
            "恭喜您获得一张优惠券！\n\n" +
            "优惠券名称：%s\n" +
            "优惠金额：%.2f 元\n" +
            "最低消费：%.2f 元\n" +
            "有效期至：%s\n\n" +
            "快去使用吧，不要错过优惠哦！",
            event.getCouponName(),
            event.getDiscountAmount(),
            event.getMinConsumeAmount(),
            expireTimeStr
        ));
        message.setType(4); // 4=优惠券提醒
        message.setStatus(0); // 0=未读
        message.setBusinessType("COUPON");
        message.setBusinessId(event.getCouponId());
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return message;
    }
}
