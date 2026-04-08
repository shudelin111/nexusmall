package com.nexusmall.notification.infrastructure.messaging;

import com.nexusmall.notification.application.service.IdempotencyService;
import com.nexusmall.notification.domain.entity.NotificationMessage;
import com.nexusmall.notification.domain.event.OrderStatusChangedEvent;
import com.nexusmall.notification.infrastructure.persistence.mapper.NotificationMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单状态变更事件监听器
 * <p>
 * 业界标准（Saga Pattern + Event Sourcing）：
 * - 监听 Order 服务发送的订单状态变更事件
 * - 自动向用户发送订单状态通知
 * - 支持分布式事务的最终一致性
 * - 符合微服务架构的松耦合原则
 * - 实现业务流程的异步解耦
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "ORDER_STATUS_CHANGED_TOPIC",
    consumerGroup = "notification-service-order-status-consumer-group",
    consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
    messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
public class OrderStatusChangedEventListener implements RocketMQListener<OrderStatusChangedEvent> {

    private final NotificationMessageMapper notificationMessageMapper;
    private final IdempotencyService idempotencyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(OrderStatusChangedEvent event) {
        log.info("收到订单状态变更事件，orderId: {}, oldStatus: {}, newStatus: {}, eventId: {}", 
                event.getOrderId(), event.getOldStatus(), event.getNewStatus(), event.getEventId());

        try {
            // 1. 幂等性校验：使用 Redis SETNX 原子操作（过期时间7天）
            if (!idempotencyService.tryProcess(event.getEventId(), 7 * 24 * 3600)) {
                log.warn("事件已处理，跳过重复消费，eventId: {}", event.getEventId());
                return;
            }

            // 2. 构建订单状态通知
            NotificationMessage message = buildOrderStatusNotification(event);

            // 3. 保存站内消息
            notificationMessageMapper.insert(message);

            log.info("订单状态通知发送成功，orderId: {}, messageId: {}", 
                    event.getOrderId(), message.getId());

        } catch (Exception e) {
            log.error("处理订单状态变更事件失败，orderId: {}, eventId: {}", 
                    event.getOrderId(), event.getEventId(), e);
            throw e; // 抛出异常触发 RocketMQ 重试机制
        }
    }

    /**
     * 构建订单状态通知
     */
    private NotificationMessage buildOrderStatusNotification(OrderStatusChangedEvent event) {
        String title = getOrderStatusTitle(event.getNewStatus());
        String content = buildOrderStatusContent(event);

        NotificationMessage message = new NotificationMessage();
        message.setMemberId(event.getUserId());
        message.setTitle(title);
        message.setContent(content);
        message.setType(2); // 2=订单状态
        message.setStatus(0); // 0=未读
        message.setBusinessType("ORDER");
        message.setBusinessId(event.getOrderId());
        message.setCreateTime(LocalDateTime.now());
        message.setUpdateTime(LocalDateTime.now());
        return message;
    }

    /**
     * 获取订单状态标题
     */
    private String getOrderStatusTitle(Integer status) {
        switch (status) {
            case 1: return "订单已提交";
            case 2: return "订单已支付";
            case 3: return "订单已发货";
            case 4: return "订单已完成";
            case 5: return "订单已取消";
            case 6: return "订单已退款";
            default: return "订单状态更新";
        }
    }

    /**
     * 构建订单状态内容
     */
    private String buildOrderStatusContent(OrderStatusChangedEvent event) {
        return String.format(
            "您的订单状态已更新\n\n" +
            "订单号：%s\n" +
            "状态：%s\n\n" +
            "如有疑问，请联系客服。",
            event.getOrderSn(),
            event.getStatusDesc() != null ? event.getStatusDesc() : "未知状态"
        );
    }
}
