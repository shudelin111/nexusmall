package com.nexusmall.promotion.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券响应VO
 * <p>
 * 业界标准：返回给前端的优惠券信息，包含可用状态判断
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "用户优惠券响应VO")
public class UserCouponVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 领取记录ID
     */
    @Schema(description = "领取记录ID")
    private Long id;

    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID")
    private Long couponId;

    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称")
    private String couponName;

    /**
     * 优惠类型描述
     */
    @Schema(description = "优惠类型描述")
    private String couponTypeDesc;

    /**
     * 面值/折扣率
     */
    @Schema(description = "面值/折扣率")
    private BigDecimal value;

    /**
     * 最低消费金额
     */
    @Schema(description = "最低消费金额")
    private BigDecimal minAmount;

    /**
     * 最高优惠金额
     */
    @Schema(description = "最高优惠金额")
    private BigDecimal maxDiscount;

    /**
     * 使用状态描述
     */
    @Schema(description = "使用状态描述")
    private String useStatusDesc;

    /**
     * 有效期开始时间
     */
    @Schema(description = "有效期开始时间")
    private LocalDateTime validStart;

    /**
     * 有效期结束时间
     */
    @Schema(description = "有效期结束时间")
    private LocalDateTime validEnd;

    /**
     * 是否可用（未使用且未过期）
     */
    @Schema(description = "是否可用")
    private Boolean available;

    /**
     * 使用的订单ID
     */
    @Schema(description = "使用的订单ID")
    private Long orderId;

    /**
     * 使用时间
     */
    @Schema(description = "使用时间")
    private LocalDateTime useTime;

    /**
     * 领取时间
     */
    @Schema(description = "领取时间")
    private LocalDateTime receiveTime;
}
