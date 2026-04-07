package com.nexusmall.payment.adapter.impl;

import com.alibaba.fastjson2.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.nexusmall.payment.domain.port.out.PayChannelAdapter;
import com.nexusmall.payment.config.AlipayConfig;
import com.nexusmall.payment.domain.constants.PayChannelCode;
import com.nexusmall.payment.domain.model.entity.PayOrder;
import com.nexusmall.payment.domain.model.enums.PayStatusEnum;
import com.nexusmall.payment.interfaces.dto.request.CreatePayOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝支付适配器
 * <p>
 * 集成支付宝官方SDK，实现真实支付功能
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayAdapter implements PayChannelAdapter {

    private final AlipayConfig alipayConfig;
    
    /**
     * 支付宝客户端(单例)
     */
    private AlipayClient alipayClient;

    @Override
    public String getChannelCode() {
        return PayChannelCode.ALIPAY;
    }

    @Override
    public String createPayment(CreatePayOrderRequest request, PayOrder payOrder) {
        log.info("【支付宝】创建支付单，paymentNo={}, amount={}", payOrder.getPaymentNo(), payOrder.getPayAmount());

        try {
            // 初始化支付宝客户端
            if (alipayClient == null) {
                synchronized (this) {
                    if (alipayClient == null) {
                        alipayClient = new DefaultAlipayClient(
                            alipayConfig.getGatewayUrl(),
                            alipayConfig.getAppId(),
                            alipayConfig.getMerchantPrivateKey(),
                            alipayConfig.getFormat(),
                            alipayConfig.getCharset(),
                            alipayConfig.getAlipayPublicKey(),
                            alipayConfig.getSignType()
                        );
                    }
                }
            }

            // 构建请求
            AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
            alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());
            alipayRequest.setReturnUrl(alipayConfig.getReturnUrl());

            // 设置业务参数
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(payOrder.getPaymentNo());
            model.setTotalAmount(payOrder.getPayAmount().toString());
            model.setSubject(request.getSubject());
            model.setBody(request.getBody());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            model.setTimeoutExpress("30m"); // 30分钟过期
            
            alipayRequest.setBizModel(model);

            // 调用支付宝接口
            AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);
            
            if (!response.isSuccess()) {
                log.error("【支付宝】创建支付单失败，code={}, msg={}", response.getCode(), response.getMsg());
                throw new RuntimeException("支付宝创建支付单失败: " + response.getSubMsg());
            }

            // 返回支付表单HTML
            String formHtml = response.getBody();
            log.info("【支付宝】支付单创建成功，paymentNo={}", payOrder.getPaymentNo());
            
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("success", true);
            resultData.put("paymentNo", payOrder.getPaymentNo());
            resultData.put("amount", payOrder.getPayAmount());
            resultData.put("subject", request.getSubject());
            resultData.put("channel", PayChannelCode.ALIPAY);
            resultData.put("formHtml", formHtml);
            resultData.put("expireTime", 1800);

            return JSON.toJSONString(resultData);
        } catch (AlipayApiException e) {
            log.error("【支付宝】调用支付宝接口异常", e);
            throw new RuntimeException("支付宝接口调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【支付宝】创建支付单失败", e);
            throw new RuntimeException("支付宝创建支付单失败: " + e.getMessage());
        }
    }

    @Override
    public Integer queryPayment(String tradeNo) {
        log.info("【支付宝】查询支付结果，tradeNo={}", tradeNo);

        try {
            // 初始化客户端
            initAlipayClient();

            // 构建查询请求
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(tradeNo);
            request.setBizModel(model);

            // 调用支付宝接口
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            
            if (!response.isSuccess()) {
                log.error("【支付宝】查询失败，code={}, msg={}", response.getCode(), response.getMsg());
                return PayStatusEnum.FAILED.getCode();
            }

            // 根据交易状态返回
            String tradeStatus = response.getTradeStatus();
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                log.info("【支付宝】查询结果：交易成功");
                return PayStatusEnum.SUCCESS.getCode();
            } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                log.info("【支付宝】查询结果：等待买家付款");
                return PayStatusEnum.WAITING.getCode();
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                log.info("【支付宝】查询结果：交易关闭");
                return PayStatusEnum.CLOSED.getCode();
            } else {
                log.warn("【支付宝】查询结果：未知状态 {}", tradeStatus);
                return PayStatusEnum.FAILED.getCode();
            }
        } catch (AlipayApiException e) {
            log.error("【支付宝】查询支付结果异常", e);
            throw new RuntimeException("支付宝查询失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【支付宝】查询支付结果失败", e);
            throw new RuntimeException("支付宝查询支付结果失败: " + e.getMessage());
        }
    }

    @Override
    public boolean handleCallback(Object callbackData) {
        log.info("【支付宝】处理支付回调，data={}", JSON.toJSONString(callbackData));

        try {
            // 验证回调数据有效性
            if (callbackData == null || !(callbackData instanceof Map)) {
                log.error("【支付宝】回调数据格式错误");
                return false;
            }
            
            Map<String, String> params = (Map<String, String>) callbackData;
            
            // 验证签名(真实调用支付宝SDK)
            boolean signVerified = AlipaySignature.rsaCheckV1(
                params,
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getCharset(),
                alipayConfig.getSignType()
            );
            
            if (!signVerified) {
                log.error("【支付宝】签名验证失败");
                return false;
            }
            
            log.info("【支付宝】签名验证通过");
            
            // 获取交易状态
            String tradeStatus = params.get("trade_status");
            
            // 判断交易状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                log.info("【支付宝】回调处理成功：交易已完成");
                return true;
            } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                log.info("【支付宝】回调处理：等待买家付款");
                return false;
            } else {
                log.warn("【支付宝】回调处理：交易状态异常，status={}", tradeStatus);
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("【支付宝】签名验证异常", e);
            return false;
        } catch (Exception e) {
            log.error("【支付宝】处理支付回调失败", e);
            return false;
        }
    }

    @Override
    public boolean refund(String refundNo, String tradeNo, String refundAmount, String reason) {
        log.info("【支付宝】执行退款，refundNo={}, tradeNo={}, amount={}, reason={}", 
                refundNo, tradeNo, refundAmount, reason);

        try {
            // 初始化客户端
            initAlipayClient();

            // 参数校验
            if (refundNo == null || refundNo.isEmpty()) {
                throw new IllegalArgumentException("退款单号不能为空");
            }
            if (tradeNo == null || tradeNo.isEmpty()) {
                throw new IllegalArgumentException("交易号不能为空");
            }
            if (refundAmount == null || refundAmount.isEmpty()) {
                throw new IllegalArgumentException("退款金额不能为空");
            }
            
            // 构建退款请求
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(tradeNo);
            model.setRefundAmount(refundAmount);
            model.setRefundReason(reason);
            model.setOutRequestNo(refundNo); // 部分退款时需要
            
            request.setBizModel(model);

            // 调用支付宝接口
            AlipayTradeRefundResponse response = alipayClient.execute(request);
            
            if (!response.isSuccess()) {
                log.error("【支付宝】退款失败，code={}, msg={}", response.getCode(), response.getMsg());
                throw new RuntimeException("支付宝退款失败: " + response.getSubMsg());
            }
            
            log.info("【支付宝】退款成功，refundNo={}", refundNo);
            return true;
        } catch (IllegalArgumentException e) {
            log.error("【支付宝】退款参数错误", e);
            throw e;
        } catch (AlipayApiException e) {
            log.error("【支付宝】退款接口调用异常", e);
            throw new RuntimeException("支付宝退款接口调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【支付宝】执行退款失败", e);
            throw new RuntimeException("支付宝退款失败: " + e.getMessage());
        }
    }

    @Override
    public boolean closeOrder(String tradeNo) {
        log.info("【支付宝】关闭订单，tradeNo={}", tradeNo);

        try {
            // 初始化客户端
            initAlipayClient();

            // 参数校验
            if (tradeNo == null || tradeNo.isEmpty()) {
                throw new IllegalArgumentException("交易号不能为空");
            }
            
            // 构建关闭订单请求
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(tradeNo);
            request.setBizModel(model);

            // 调用支付宝接口
            AlipayTradeCloseResponse response = alipayClient.execute(request);
            
            if (!response.isSuccess()) {
                log.error("【支付宝】关闭订单失败，code={}, msg={}", response.getCode(), response.getMsg());
                return false;
            }
            
            log.info("【支付宝】订单关闭成功，tradeNo={}", tradeNo);
            return true;
        } catch (IllegalArgumentException e) {
            log.error("【支付宝】关闭订单参数错误", e);
            throw e;
        } catch (AlipayApiException e) {
            log.error("【支付宝】关闭订单接口调用异常", e);
            throw new RuntimeException("支付宝关闭订单接口调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("【支付宝】关闭订单失败", e);
            throw new RuntimeException("支付宝关闭订单失败: " + e.getMessage());
        }
    }
    
    /**
     * 初始化支付宝客户端(懒加载)
     */
    private void initAlipayClient() {
        if (alipayClient == null) {
            synchronized (this) {
                if (alipayClient == null) {
                    alipayClient = new DefaultAlipayClient(
                        alipayConfig.getGatewayUrl(),
                        alipayConfig.getAppId(),
                        alipayConfig.getMerchantPrivateKey(),
                        alipayConfig.getFormat(),
                        alipayConfig.getCharset(),
                        alipayConfig.getAlipayPublicKey(),
                        alipayConfig.getSignType()
                    );
                    log.info("【支付宝】客户端初始化成功，gatewayUrl={}", alipayConfig.getGatewayUrl());
                }
            }
        }
    }
}
