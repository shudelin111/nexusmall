package com.nexusmall.notification.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单状态变更领域事件
 * <p>
 * 业界标准（Saga Pattern + Event Sourcing）：
 * - Order 服务订单状态变更时发布此事件
 * - Notification 服务监听并发送订单状态通知
 * - 支持分布式事务的最终一致性
 * - 符合微服务架构的松耦合原则
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 旧状态
     */
    private Integer oldStatus;

    /**
     * 新状态
     */
    private Integer newStatus;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 变更时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 事件ID（用于幂等性校验）
     */
    private String eventId;
}
