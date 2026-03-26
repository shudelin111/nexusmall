package com.nexusmall.order.service;

import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.common.constant.ErrorMessageConstants;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.OrderException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 消息生产者
 */
@Slf4j
@Service
public class RocketMQProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送订单取消延迟消息
     * 
     * @param orderId 订单 ID
     * @param delayLevel 延迟级别（1-18），17 表示 30 分钟
     */
    public void sendOrderCancelDelayMessage(Long orderId, int delayLevel) {
        log.info("发送订单取消延迟消息，orderId: {}, 延迟级别：{}", orderId, delayLevel);
        
        try {
            // 构建消息体
            Message<Long> message = MessageBuilder.withPayload(orderId).build();
            
            // 发送到订单 Topic，使用订单取消 Tag
            String destination = MQConstants.Order.TOPIC + ":" + MQConstants.Order.CANCEL_TAG;
            
            // 发送延迟消息
            rocketMQTemplate.syncSend(destination, message, 3000, delayLevel);
            
            log.info("订单取消延迟消息发送成功，orderId: {}", orderId);
        } catch (Exception e) {
            log.error("发送订单取消延迟消息失败，orderId: {}", orderId, e);
            throw new OrderException(CommonResultCode.MQ_SEND_FAILED.getErrorCode(), CommonResultCode.MQ_SEND_FAILED.getMessage(), e);
        }
    }

    /**
     * 发送普通消息
     * 
     * @param topic 主题
     * @param tag 标签
     * @param payload 消息内容
     */
    public void sendNormalMessage(String topic, String tag, Object payload) {
        log.info("发送普通消息，topic: {}, tag: {}, payload: {}", topic, tag, payload);
        
        try {
            String destination = topic + ":" + tag;
            rocketMQTemplate.convertAndSend(destination, payload);
            
            log.info("普通消息发送成功，topic: {}, tag: {}", topic, tag);
        } catch (Exception e) {
            log.error("发送普通消息失败，topic: {}, tag: {}", topic, tag, e);
            throw new OrderException(CommonResultCode.SYSTEM_ERROR.getErrorCode(), 
                    ErrorMessageConstants.Order.MQ_SEND_MESSAGE_FAILED, e);
        }
    }

}
