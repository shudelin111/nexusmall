package com.nexusmall.payment.adapter;

import com.nexusmall.payment.entity.PayOrder;
import com.nexusmall.payment.vo.request.CreatePayOrderRequest;

/**
 * 支付渠道适配器接口
 * <p>
 * 策略模式：每个支付渠道实现此接口
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface PayChannelAdapter {

    /**
     * 获取渠道编码
     *
     * @return 渠道编码（ALIPAY/WECHAT/UNIONPAY）
     */
    String getChannelCode();

    /**
     * 创建支付单并调用第三方支付接口
     *
     * @param request 创建支付单请求
     * @param payOrder 支付单实体
     * @return 支付表单HTML或二维码URL
     */
    String createPayment(CreatePayOrderRequest request, PayOrder payOrder);

    /**
     * 查询支付结果
     *
     * @param tradeNo 第三方交易号
     * @return 支付状态
     */
    Integer queryPayment(String tradeNo);

    /**
     * 处理支付回调
     *
     * @param callbackData 回调数据
     * @return 处理结果
     */
    boolean handleCallback(Object callbackData);

    /**
     * 退款
     *
     * @param refundNo 退款单号
     * @param tradeNo 原交易号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 退款结果
     */
    boolean refund(String refundNo, String tradeNo, String refundAmount, String reason);

    /**
     * 关闭订单
     *
     * @param tradeNo 第三方交易号
     * @return 关闭结果
     */
    boolean closeOrder(String tradeNo);
}
