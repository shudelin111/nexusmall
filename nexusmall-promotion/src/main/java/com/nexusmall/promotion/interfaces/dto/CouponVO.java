package com.nexusmall.promotion.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券响应VO
 * <p>
 * 业界标准：使用VO返回数据，隐藏内部实现细节
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "优惠券响应VO")
public class CouponVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID")
    private Long id;

    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称")
    private String name;

    /**
     * 优惠类型描述
     */
    @Schema(description = "优惠类型描述")
    private String typeDesc;

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
     * 剩余库存
     */
    @Schema(description = "剩余库存")
    private Integer remainStock;

    /**
     * 每人限领数量
     */
    @Schema(description = "每人限领数量")
    private Integer perLimit;

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
     * 状态描述
     */
    @Schema(description = "状态描述")
    private String statusDesc;
}
