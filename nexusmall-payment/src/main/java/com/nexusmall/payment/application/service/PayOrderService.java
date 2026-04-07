package com.nexusmall.payment.application.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.payment.domain.port.out.PayChannelAdapter;
import com.nexusmall.payment.adapter.outbound.persistence.PayOrderMapper;
import com.nexusmall.payment.domain.model.entity.PayOrder;
import com.nexusmall.payment.domain.model.enums.PayStatusEnum;
import com.nexusmall.payment.interfaces.exception.PaymentException;
import com.nexusmall.payment.interfaces.dto.request.CreatePayOrderRequest;
import com.nexusmall.payment.interfaces.dto.response.PayOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 支付单服务类
 * <p>
 * 核心业务逻辑：创建支付单、查询支付单、处理回调等
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayOrderService {

    private final PayOrderMapper payOrderMapper;
    private final List<PayChannelAdapter> channelAdapters;

    /**
     * 渠道适配器缓存
     */
    private Map<String, PayChannelAdapter> adapterMap;

    /**
     * 初始化渠道适配器映射
     */
    private void initAdapterMap() {
        if (adapterMap == null) {
            adapterMap = new HashMap<>();
            for (PayChannelAdapter adapter : channelAdapters) {
                adapterMap.put(adapter.getChannelCode(), adapter);
            }
        }
    }

    /**
     * 创建支付单
     *
     * @param request 创建支付单请求
     * @return 支付表单HTML或二维码URL
     */
    @Transactional(rollbackFor = Exception.class)
    public String createPayOrder(CreatePayOrderRequest request) {
        log.info("【创建支付单】开始，orderNo={}, channel={}", request.getOrderNo(), request.getChannelCode());

        // 1. 检查订单是否已存在支付单
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getOrderNo, request.getOrderNo())
                .eq(PayOrder::getStatus, PayStatusEnum.WAITING.getCode());
        PayOrder existOrder = payOrderMapper.selectOne(wrapper);
        if (existOrder != null) {
            log.warn("【创建支付单】订单已存在待支付单，paymentNo={}", existOrder.getPaymentNo());
            return getChannelAdapter(request.getChannelCode()).createPayment(request, existOrder);
        }

        // 2. 生成支付单号
        String paymentNo = generatePaymentNo();

        // 3. 构建支付单实体
        PayOrder payOrder = new PayOrder();
        BeanUtils.copyProperties(request, payOrder);
        payOrder.setPaymentNo(paymentNo);
        payOrder.setStatus(PayStatusEnum.WAITING.getCode());
        payOrder.setRefundAmount(java.math.BigDecimal.ZERO);
        
        // 设置过期时间（30分钟）
        payOrder.setExpireTime(LocalDateTime.now().plusMinutes(30));

        // 4. 保存支付单
        int rows = payOrderMapper.insert(payOrder);
        if (rows <= 0) {
            throw new PaymentException("创建支付单失败");
        }

        log.info("【创建支付单】成功，paymentNo={}", paymentNo);

        // 5. 调用第三方支付接口
        return getChannelAdapter(request.getChannelCode()).createPayment(request, payOrder);
    }

    /**
     * 查询支付单
     *
     * @param paymentNo 支付单号
     * @return 支付单响应
     */
    public PayOrderResponse queryPayOrder(String paymentNo) {
        log.info("【查询支付单】paymentNo={}", paymentNo);

        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getPaymentNo, paymentNo);
        PayOrder payOrder = payOrderMapper.selectOne(wrapper);

        if (payOrder == null) {
            throw new PaymentException("支付单不存在");
        }

        // 转换为响应对象
        PayOrderResponse response = new PayOrderResponse();
        BeanUtils.copyProperties(payOrder, response);
        response.setStatusDesc(PayStatusEnum.getByCode(payOrder.getStatus()).getDesc());

        return response;
    }

    /**
     * 根据订单号查询支付单
     *
     * @param orderNo 订单号
     * @return 支付单响应
     */
    public PayOrderResponse queryByOrderNo(String orderNo) {
        log.info("【查询支付单】orderNo={}", orderNo);

        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getOrderNo, orderNo)
                .orderByDesc(PayOrder::getCreateTime)
                .last("LIMIT 1");
        PayOrder payOrder = payOrderMapper.selectOne(wrapper);

        if (payOrder == null) {
            throw new PaymentException("支付单不存在");
        }

        PayOrderResponse response = new PayOrderResponse();
        BeanUtils.copyProperties(payOrder, response);
        response.setStatusDesc(PayStatusEnum.getByCode(payOrder.getStatus()).getDesc());

        return response;
    }

    /**
     * 处理支付回调
     * <p>
     * 业界标准：
     * 1. 验证签名(防止伪造回调)
     * 2. 幂等性处理(同一回调可能多次发送)
     * 3. 更新支付单状态
     * 4. 发送支付成功消息到 MQ(触发订单状态更新)
     * </p>
     *
     * @param channelCode 渠道编码
     * @param callbackData 回调数据
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleCallback(String channelCode, Object callbackData) {
        log.info("【支付回调】channel={}, data={}", channelCode, callbackData);

        try {
            // 1. 获取渠道适配器
            PayChannelAdapter adapter = getChannelAdapter(channelCode);

            // 2. 验证签名并解析数据(适配器内部会验证)
            boolean verified = adapter.handleCallback(callbackData);
            if (!verified) {
                log.error("【支付回调】签名验证失败");
                return false;
            }

            // 3. 从回调数据中提取 paymentNo 和 tradeNo
            // 真实场景需要根据实际回调数据结构解析:
            // Map<String, String> params = (Map<String, String>) callbackData;
            // String paymentNo = params.get("out_trade_no");
            // String tradeNo = params.get("trade_no");  // 支付宝的交易号
            // String transactionId = params.get("transaction_id");  // 微信的交易号
            
            // 模拟提取数据(真实环境需要实现解析逻辑)
            String paymentNo = extractPaymentNo(callbackData);
            String tradeNo = extractTradeNo(callbackData, channelCode);

            // 4. 查询支付单
            LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(PayOrder::getPaymentNo, paymentNo);
            PayOrder payOrder = payOrderMapper.selectOne(wrapper);

            if (payOrder == null) {
                log.error("【支付回调】支付单不存在，paymentNo={}", paymentNo);
                return false;
            }

            // 5. 幂等性检查：如果已经是成功状态，直接返回成功
            if (PayStatusEnum.SUCCESS.getCode().equals(payOrder.getStatus())) {
                log.info("【支付回调】支付单已经是成功状态，无需重复处理，paymentNo={}", paymentNo);
                return true;
            }

            // 6. 更新支付单状态
            payOrder.setStatus(PayStatusEnum.SUCCESS.getCode());
            payOrder.setTradeNo(tradeNo);
            payOrder.setPayTime(LocalDateTime.now());
            payOrder.setCallbackContent(JSON.toJSONString(callbackData));
            payOrder.setCallbackTime(LocalDateTime.now());
            payOrderMapper.updateById(payOrder);

            // 7. 发送支付成功消息到 MQ(触发订单服务更新订单状态)
            // sendPaymentSuccessMessage(payOrder);

            log.info("【支付回调】处理成功，paymentNo={}, tradeNo={}", paymentNo, tradeNo);
            return true;
        } catch (Exception e) {
            log.error("【支付回调】处理异常", e);
            return false;
        }
    }

    /**
     * 从回调数据中提取支付单号
     *
     * @param callbackData 回调数据
     * @return 支付单号
     */
    private String extractPaymentNo(Object callbackData) {
        // 真实实现需要根据实际回调数据结构解析
        // 示例：Map<String, String> params = (Map<String, String>) callbackData;
        // return params.get("out_trade_no");
        
        // 模拟实现
        return "PAY" + System.currentTimeMillis();
    }

    /**
     * 从回调数据中提取交易号
     *
     * @param callbackData 回调数据
     * @param channelCode 渠道编码
     * @return 交易号
     */
    private String extractTradeNo(Object callbackData, String channelCode) {
        // 真实实现需要根据实际回调数据结构解析
        // 支付宝：params.get("trade_no")
        // 微信：params.get("transaction_id")
        
        // 模拟实现
        return "TRADE" + System.currentTimeMillis();
    }

    /**
     * 关闭过期支付单
     *
     * @return 关闭数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int closeExpiredOrders() {
        log.info("【关闭过期订单】开始");

        // 查询已过期的待支付订单
        LambdaQueryWrapper<PayOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PayOrder::getStatus, PayStatusEnum.WAITING.getCode())
                .lt(PayOrder::getExpireTime, LocalDateTime.now());

        List<PayOrder> expiredOrders = payOrderMapper.selectList(wrapper);
        if (expiredOrders.isEmpty()) {
            log.info("【关闭过期订单】无过期订单");
            return 0;
        }

        int count = 0;
        for (PayOrder order : expiredOrders) {
            // 调用第三方关闭订单
            if (order.getTradeNo() != null) {
                PayChannelAdapter adapter = getChannelAdapter(order.getChannelCode());
                adapter.closeOrder(order.getTradeNo());
            }

            // 更新本地状态
            order.setStatus(PayStatusEnum.CLOSED.getCode());
            payOrderMapper.updateById(order);
            count++;
        }

        log.info("【关闭过期订单】完成，关闭{}个订单", count);
        return count;
    }

    /**
     * 生成支付单号
     *
     * @return 支付单号
     */
    private String generatePaymentNo() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 获取渠道适配器
     *
     * @param channelCode 渠道编码
     * @return 渠道适配器
     */
    private PayChannelAdapter getChannelAdapter(String channelCode) {
        initAdapterMap();
        PayChannelAdapter adapter = adapterMap.get(channelCode);
        if (adapter == null) {
            throw new PaymentException("不支持的支付渠道：" + channelCode);
        }
        return adapter;
    }
}
