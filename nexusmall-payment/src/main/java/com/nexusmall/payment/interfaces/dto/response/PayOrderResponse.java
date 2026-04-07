package com.nexusmall.payment.interfaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单响应
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "支付单响应")
public class PayOrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 支付单号
     */
    @Schema(description = "支付单号", example = "PAY202604060001")
    private String paymentNo;

    /**
     * 订单号
     */
    @Schema(description = "订单号", example = "ORD202604060001")
    private String orderNo;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 会员ID
     */
    @Schema(description = "会员ID", example = "2001")
    private Long memberId;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额", example = "99.90")
    private BigDecimal totalAmount;

    /**
     * 实际支付金额
     */
    @Schema(description = "实际支付金额", example = "89.90")
    private BigDecimal payAmount;

    /**
     * 优惠金额
     */
    @Schema(description = "优惠金额", example = "10.00")
    private BigDecimal discountAmount;

    /**
     * 已退款金额
     */
    @Schema(description = "已退款金额", example = "0.00")
    private BigDecimal refundAmount;

    /**
     * 支付渠道编码
     */
    @Schema(description = "支付渠道编码", example = "ALIPAY")
    private String channelCode;

    /**
     * 支付渠道名称
     */
    @Schema(description = "支付渠道名称", example = "支付宝")
    private String channelName;

    /**
     * 第三方交易号
     */
    @Schema(description = "第三方交易号", example = "2026040622001234567890")
    private String tradeNo;

    /**
     * 支付状态：0=待支付，1=支付中，2=支付成功，3=支付失败，4=已关闭，5=已退款
     */
    @Schema(description = "支付状态", example = "0")
    private Integer status;

    /**
     * 支付状态描述
     */
    @Schema(description = "支付状态描述", example = "待支付")
    private String statusDesc;

    /**
     * 支付完成时间
     */
    @Schema(description = "支付完成时间", example = "2026-04-06 10:30:00")
    private LocalDateTime payTime;

    /**
     * 支付过期时间
     */
    @Schema(description = "支付过期时间", example = "2026-04-06 11:00:00")
    private LocalDateTime expireTime;

    /**
     * 商品描述
     */
    @Schema(description = "商品描述", example = "测试商品")
    private String subject;

    /**
     * 商品详情
     */
    @Schema(description = "商品详情", example = "这是一个测试商品的详细描述")
    private String body;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-04-06 10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-04-06 10:00:00")
    private LocalDateTime updateTime;
}
