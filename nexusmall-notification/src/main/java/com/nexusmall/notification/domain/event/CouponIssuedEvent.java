package com.nexusmall.notification.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券发放领域事件
 * <p>
 * 业界标准（Domain Event Pattern）：
 * - Promotion 服务发放优惠券后发布此事件
 * - Notification 服务监听并发送优惠券到账通知
 * - 支持多渠道通知（站内信、短信、推送）
 * - 符合事件溯源（Event Sourcing）最佳实践
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponIssuedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 优惠券 ID
     */
    private Long couponId;

    /**
     * 优惠券名称
     */
    private String couponName;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 最低消费金额
     */
    private BigDecimal minConsumeAmount;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 发放时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 事件ID（用于幂等性校验）
     */
    private String eventId;
}
