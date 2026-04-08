package com.nexusmall.order.interfaces.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建订单请求 VO
 */
@Data
public class OrderCreateRequest {

    /**
     * 用户 ID
     */
    @NotNull(message = "用户 ID 不能为空")
    private Long memberId;

    /**
     * 商品 ID
     */
    @NotNull(message = "商品 ID 不能为空")
    private Long productId;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String skuName;

    /**
     * 商品价格
     */
    @NotNull(message = "商品价格不能为空")
    @Min(value = 0, message = "商品价格必须大于等于 0")
    private BigDecimal price;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量至少为 1")
    private Integer count;

    /**
     * 订单总金额
     */
    @NotNull(message = "订单总金额不能为空")
    @Min(value = 0, message = "订单总金额必须大于等于 0")
    private BigDecimal totalAmount;

    /**
     * 实付金额
     */
    @NotNull(message = "实付金额不能为空")
    @Min(value = 0, message = "实付金额必须大于等于 0")
    private BigDecimal payAmount;

    /**
     * 运费金额
     */
    @NotNull(message = "运费金额不能为空")
    @Min(value = 0, message = "运费金额必须大于等于 0")
    private BigDecimal freightAmount = BigDecimal.ZERO;

    /**
     * 促销优惠金额
     */
    @NotNull(message = "促销优惠金额不能为空")
    @Min(value = 0, message = "促销优惠金额必须大于等于 0")
    private BigDecimal promotionAmount = BigDecimal.ZERO;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    /**
     * 收货人地址
     */
    @NotBlank(message = "收货人地址不能为空")
    private String receiverAddress;

    /**
     * 支付方式：1-微信，2-支付宝，3-银行卡
     */
    @NotNull(message = "支付方式不能为空")
    private Integer paymentType;

    /**
     * 订单备注
     */
    private String remark;
}
