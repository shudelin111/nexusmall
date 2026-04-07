package com.nexusmall.payment.adapter.inbound.mq;

import com.alibaba.fastjson2.JSON;
import com.nexusmall.payment.application.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付结果监听器
 * <p>
 * 监听订单服务发送的支付结果消息
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "PAYMENT_RESULT_TOPIC",
        consumerGroup = "payment_result_consumer_group"
)
public class PayResultListener implements RocketMQListener<String> {

    private final PayOrderService payOrderService;

    @Override
    public void onMessage(String message) {
        log.info("【支付结果监听】收到消息：{}", message);

        try {
            // 解析消息
            Map<String, Object> data = JSON.parseObject(message, Map.class);
            
            String orderNo = (String) data.get("orderNo");
            String paymentNo = (String) data.get("paymentNo");
            String tradeNo = (String) data.get("tradeNo");
            Integer status = (Integer) data.get("status");

            log.info("【支付结果监听】处理支付结果，orderNo={}, paymentNo={}, status={}", 
                    orderNo, paymentNo, status);

            // TODO: 根据业务需求处理支付结果
            // 1. 更新支付单状态
            // 2. 发送通知给用户
            // 3. 触发后续业务流程（如发货）

            log.info("【支付结果监听】处理成功");
        } catch (Exception e) {
            log.error("【支付结果监听】处理失败", e);
            // RocketMQ 会自动重试
            throw new RuntimeException("支付结果处理失败", e);
        }
    }
}
