package com.nexusmall.promotion.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实�?
 * <p>
 * 业界标准�?
 * - 支持多种优惠类型（满减、折扣、立减）
 * - 支持使用范围（全场、指定分类、指定商品）
 * - 支持领取限制（每人限领、总库存）
 * - 支持有效期设�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("promotion_coupon")
@Schema(description = "优惠券实�?)
public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "优惠券ID")
    private Long id;

    /**
     * 优惠券名�?
     */
    @Schema(description = "优惠券名�?)
    private String name;

    /**
     * 优惠券编码（唯一标识�?
     */
    @Schema(description = "优惠券编�?)
    private String code;

    /**
     * 优惠类型�?-满减 2-折扣 3-立减
     */
    @Schema(description = "优惠类型�?-满减 2-折扣 3-立减")
    private Integer type;

    /**
     * 面�?折扣率（满减为金额，折扣为百分比�?0表示8折）
     */
    @Schema(description = "面�?折扣�?)
    private BigDecimal value;

    /**
     * 最低消费金额（满减门槛�?
     */
    @Schema(description = "最低消费金�?)
    private BigDecimal minAmount;

    /**
     * 最高优惠金额（封顶�?
     */
    @Schema(description = "最高优惠金�?)
    private BigDecimal maxDiscount;

    /**
     * 使用范围�?-全场 1-指定分类 2-指定商品
     */
    @Schema(description = "使用范围�?-全场 1-指定分类 2-指定商品")
    private Integer scope;

    /**
     * 适用范围JSON（分类ID列表或商品ID列表�?
     */
    @Schema(description = "适用范围JSON")
    private String scopeData;

    /**
     * 总库�?
     */
    @Schema(description = "总库�?)
    private Integer totalStock;

    /**
     * 已领取数�?
     */
    @Schema(description = "已领取数�?)
    private Integer receivedCount;

    /**
     * 每人限领数量�?表示不限制）
     */
    @Schema(description = "每人限领数量")
    private Integer perLimit;

    /**
     * 有效期类型：1-固定日期 2-领取后N�?
     */
    @Schema(description = "有效期类型：1-固定日期 2-领取后N�?)
    private Integer validType;

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
     * 领取后有效天�?
     */
    @Schema(description = "领取后有效天�?)
    private Integer validDays;

    /**
     * 状态：0-未开�?1-进行�?2-已结�?3-已下�?
     */
    @Schema(description = "状态：0-未开�?1-进行�?2-已结�?3-已下�?)
    private Integer status;

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
