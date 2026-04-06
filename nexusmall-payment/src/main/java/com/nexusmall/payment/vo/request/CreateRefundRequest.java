package com.nexusmall.payment.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建退款申请请求
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "创建退款申请请求")
public class CreateRefundRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付单号
     */
    @NotBlank(message = "支付单号不能为空")
    @Schema(description = "支付单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PAY202604060001")
    private String paymentNo;

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    @Schema(description = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "ORD202604060001")
    private String orderNo;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long userId;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    @Schema(description = "退款金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "89.90")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotBlank(message = "退款原因不能为空")
    @Schema(description = "退款原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "商品质量问题")
    private String reason;
}
