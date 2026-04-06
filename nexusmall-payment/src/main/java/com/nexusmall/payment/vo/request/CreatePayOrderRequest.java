package com.nexusmall.payment.vo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建支付单请求
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "创建支付单请求")
public class CreatePayOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 会员ID
     */
    @Schema(description = "会员ID", example = "2001")
    private Long memberId;

    /**
     * 订单总金额
     */
    @NotNull(message = "订单总金额不能为空")
    @DecimalMin(value = "0.01", message = "订单总金额必须大于0")
    @Schema(description = "订单总金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.90")
    private BigDecimal totalAmount;

    /**
     * 实际支付金额
     */
    @NotNull(message = "实际支付金额不能为空")
    @DecimalMin(value = "0.01", message = "实际支付金额必须大于0")
    @Schema(description = "实际支付金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "89.90")
    private BigDecimal payAmount;

    /**
     * 优惠金额
     */
    @Schema(description = "优惠金额", example = "10.00")
    private BigDecimal discountAmount;

    /**
     * 支付渠道编码：ALIPAY/WECHAT/UNIONPAY
     */
    @NotBlank(message = "支付渠道不能为空")
    @Schema(description = "支付渠道编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "ALIPAY")
    private String channelCode;

    /**
     * 商品描述
     */
    @NotBlank(message = "商品描述不能为空")
    @Schema(description = "商品描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试商品")
    private String subject;

    /**
     * 商品详情
     */
    @Schema(description = "商品详情", example = "这是一个测试商品的详细描述")
    private String body;

    /**
     * 客户端IP
     */
    @Schema(description = "客户端IP", example = "192.168.1.100")
    private String clientIp;

    /**
     * 用户代理
     */
    @Schema(description = "用户代理", example = "Mozilla/5.0...")
    private String userAgent;
}
