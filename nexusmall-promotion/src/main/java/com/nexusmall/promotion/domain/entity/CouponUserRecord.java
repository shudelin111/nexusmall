package com.nexusmall.promotion.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券领取记录实�?
 * <p>
 * 业界标准�?
 * - 记录每个用户的每张优惠券
 * - 支持状态流转（未使用→已锁定→已使�?已过期）
 * - 支持订单关联（核销时记录订单ID�?
 * - 支持退款回退（释放优惠券�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("promotion_coupon_user_record")
@Schema(description = "用户优惠券领取记录实�?)
public class CouponUserRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;

    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID")
    private Long couponId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 优惠券名称（冗余字段，方便查询）
     */
    @Schema(description = "优惠券名�?)
    private String couponName;

    /**
     * 优惠类型�?-满减 2-折扣 3-立减
     */
    @Schema(description = "优惠类型�?-满减 2-折扣 3-立减")
    private Integer couponType;

    /**
     * 面�?折扣�?
     */
    @Schema(description = "面�?折扣�?)
    private BigDecimal value;

    /**
     * 最低消费金�?
     */
    @Schema(description = "最低消费金�?)
    private BigDecimal minAmount;

    /**
     * 最高优惠金�?
     */
    @Schema(description = "最高优惠金�?)
    private BigDecimal maxDiscount;

    /**
     * 使用范围�?-全场 1-指定分类 2-指定商品
     */
    @Schema(description = "使用范围�?-全场 1-指定分类 2-指定商品")
    private Integer scope;

    /**
     * 适用范围JSON
     */
    @Schema(description = "适用范围JSON")
    private String scopeData;

    /**
     * 有效期开始时�?
     */
    @Schema(description = "有效期开始时�?)
    private LocalDateTime validStart;

    /**
     * 有效期结束时�?
     */
    @Schema(description = "有效期结束时�?)
    private LocalDateTime validEnd;

    /**
     * 使用状态：0-未使�?1-已使�?2-已过�?3-已锁�?
     */
    @Schema(description = "使用状态：0-未使�?1-已使�?2-已过�?3-已锁�?)
    private Integer useStatus;

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
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "领取时间")
    private LocalDateTime receiveTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @Schema(description = "逻辑删除")
    private Integer deleted;
}
