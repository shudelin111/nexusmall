package com.nexusmall.payment.vo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款响应
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "退款响应")
public class RefundResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 退款单号
     */
    @Schema(description = "退款单号", example = "REF202604060001")
    private String refundNo;

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
     * 退款金额
     */
    @Schema(description = "退款金额", example = "89.90")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @Schema(description = "退款原因", example = "商品质量问题")
    private String reason;

    /**
     * 退款状态：0=待审核，1=审核通过，2=审核拒绝，3=退款中，4=退款成功，5=退款失败
     */
    @Schema(description = "退款状态", example = "0")
    private Integer status;

    /**
     * 退款状态描述
     */
    @Schema(description = "退款状态描述", example = "待审核")
    private String statusDesc;

    /**
     * 第三方退款交易号
     */
    @Schema(description = "第三方退款交易号", example = "2026040622001234567890")
    private String refundTradeNo;

    /**
     * 退款完成时间
     */
    @Schema(description = "退款完成时间", example = "2026-04-06 15:30:00")
    private LocalDateTime refundTime;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID", example = "100")
    private Long auditorId;

    /**
     * 审核人姓名
     */
    @Schema(description = "审核人姓名", example = "管理员")
    private String auditorName;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间", example = "2026-04-06 14:00:00")
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-04-06 13:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-04-06 13:00:00")
    private LocalDateTime updateTime;
}
