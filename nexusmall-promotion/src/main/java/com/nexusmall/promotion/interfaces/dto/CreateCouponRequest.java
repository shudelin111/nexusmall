package com.nexusmall.promotion.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建优惠券请求DTO
 * <p>
 * 业界标准：使用DTO接收参数，配合JSR-303校验
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "创建优惠券请求")
public class CreateCouponRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 优惠券名称
     */
    @NotBlank(message = "优惠券名称不能为空")
    @Schema(description = "优惠券名称", required = true, example = "双11满减券")
    private String name;

    /**
     * 优惠类型：1-满减 2-折扣 3-立减
     */
    @NotNull(message = "优惠类型不能为空")
    @Schema(description = "优惠类型：1-满减 2-折扣 3-立减", required = true, example = "1")
    private Integer type;

    /**
     * 面值/折扣率
     */
    @NotNull(message = "面值/折扣率不能为空")
    @DecimalMin(value = "0.01", message = "面值/折扣率必须大于0")
    @Schema(description = "面值/折扣率", required = true, example = "50.00")
    private BigDecimal value;

    /**
     * 最低消费金额
     */
    @DecimalMin(value = "0.00", message = "最低消费金额不能为负数")
    @Schema(description = "最低消费金额", example = "100.00")
    private BigDecimal minAmount;

    /**
     * 最高优惠金额
     */
    @DecimalMin(value = "0.00", message = "最高优惠金额不能为负数")
    @Schema(description = "最高优惠金额", example = "50.00")
    private BigDecimal maxDiscount;

    /**
     * 总库存
     */
    @NotNull(message = "总库存不能为空")
    @Schema(description = "总库存", required = true, example = "1000")
    private Integer totalStock;

    /**
     * 每人限领数量
     */
    @Schema(description = "每人限领数量（0表示不限制）", example = "1")
    private Integer perLimit;

    /**
     * 有效期开始时间
     */
    @NotNull(message = "有效期开始时间不能为空")
    @Schema(description = "有效期开始时间", required = true)
    private LocalDateTime validStart;

    /**
     * 有效期结束时间
     */
    @NotNull(message = "有效期结束时间不能为空")
    @Schema(description = "有效期结束时间", required = true)
    private LocalDateTime validEnd;
}
