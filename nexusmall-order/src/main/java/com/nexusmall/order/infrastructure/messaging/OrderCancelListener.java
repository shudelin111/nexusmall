package com.nexusmall.order.infrastructure.messaging;

import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.order.application.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单取消消息消费者（监听延迟消息�?
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = MQConstants.Order.TOPIC,
    selectorExpression = MQConstants.Order.CANCEL_TAG,
    consumerGroup = MQConstants.Order.CONSUMER_GROUP
)
public class OrderCancelListener implements RocketMQListener<Long> {

    @Autowired
    private OrderService orderService;

    @Override
    public void onMessage(Long orderId) {
        log.info("收到订单取消延迟消息，orderId: {}", orderId);
        
        try {
            // 检查订单是否已支付
            // 如果未支付，取消订单
            orderService.cancelUnpaidOrder(orderId);
            
            log.info("订单取消成功，orderId: {}", orderId);
        } catch (Exception e) {
            log.error("取消订单失败，orderId: {}", orderId, e);
            // 这里可以重新投递消息或者记录到死信队列
        }
    }

}
