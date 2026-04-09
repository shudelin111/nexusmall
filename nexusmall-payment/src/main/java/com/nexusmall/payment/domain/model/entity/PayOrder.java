package com.nexusmall.payment.domain.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单实体类
 * <p>
 * 对应表：pay_order
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("pay_order")
public class PayOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 支付单号（业务唯一标识?
     */
    private String paymentNo;

    /**
     * 订单号（关联订单服务?
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 订单总金?
     */
    private BigDecimal totalAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 已退款金?
     */
    private BigDecimal refundAmount;

    /**
     * 支付渠道编码：ALIPAY/WECHAT/UNIONPAY
     */
    private String channelCode;

    /**
     * 支付渠道名称
     */
    private String channelName;

    /**
     * 第三方交易号（支付宝/微信返回?
     */
    private String tradeNo;

    /**
     * 支付状态：0=待支付，1=支付中，2=支付成功?=支付失败?=已关闭，5=已退?
     */
    private Integer status;

    /**
     * 支付完成时间
     */
    private LocalDateTime payTime;

    /**
     * 支付过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 第三方支付回调原始数?
     */
    private String callbackContent;

    /**
     * 回调时间
     */
    private LocalDateTime callbackTime;

    /**
     * 商品描述
     */
    private String subject;

    /**
     * 商品详情
     */
    private String body;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 逻辑删除?=未删除，1=已删?
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
