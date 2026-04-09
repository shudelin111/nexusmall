package com.nexusmall.payment.domain.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请实体类
 * <p>
 * 对应表：pay_refund
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("pay_refund")
public class PayRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 退款单号（业务唯一标识�?
     */
    private String refundNo;

    /**
     * 支付单号
     */
    private String paymentNo;

    /**
     * 订单�?
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款金�?
     */
    private BigDecimal refundAmount;

    /**
     * 退款原�?
     */
    private String reason;

    /**
     * 退款状态：0=待审核，1=审核通过�?=审核拒绝�?=退款中�?=退款成功，5=退款失�?
     */
    private Integer status;

    /**
     * 第三方退款交易号
     */
    private String refundTradeNo;

    /**
     * 退款完成时�?
     */
    private LocalDateTime refundTime;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核人姓�?
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 第三方回调原始数�?
     */
    private String callbackContent;

    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;

    /**
     * 逻辑删除�?=未删除，1=已删�?
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
