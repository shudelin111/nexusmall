package com.nexusmall.cart.infrastructure.messaging;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 购物车同步消息生产者
 * <p>
 * 业界标准：使用RocketMQ异步同步购物车数据到MySQL
 * - 解耦Redis和MySQL写入
 * - 提升接口响应速度
 * - 支持重试机制保证最终一致性
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CartSyncProducer {

    private final RocketMQTemplate rocketMQTemplate;

    /**
     * Topic名称
     */
    private static final String TOPIC = "CART_SYNC_TOPIC";

    /**
     * Tag名称
     */
    private static final String TAG = "cart_sync";

    /**
     * 发送购物车同步消息
     *
     * @param message 同步消息
     */
    public void sendSyncMessage(CartSyncMessage message) {
        try {
            // 生成消息ID（用于幂等性校验）
            if (message.getMessageId() == null) {
                message.setMessageId(java.util.UUID.randomUUID().toString());
            }

            // 构建完整Topic
            String destination = TOPIC + ":" + TAG;

            // 发送消息
            rocketMQTemplate.syncSend(destination, MessageBuilder.withPayload(message).build());

            log.info("【发送购物车同步消息】成功, messageId={}, userId={}, skuId={}, operationType={}",
                    message.getMessageId(), message.getUserId(), message.getSkuId(), message.getOperationType());
        } catch (Exception e) {
            log.error("【发送购物车同步消息】失败, userId={}, skuId={}",
                    message.getUserId(), message.getSkuId(), e);
            // 不抛出异常，避免影响主流程
            // 生产环境应记录到本地日志，后续补偿
        }
    }

    /**
     * 异步发送购物车同步消息
     *
     * @param message 同步消息
     */
    public void sendAsyncSyncMessage(CartSyncMessage message) {
        try {
            // 生成消息ID
            if (message.getMessageId() == null) {
                message.setMessageId(java.util.UUID.randomUUID().toString());
            }

            String destination = TOPIC + ":" + TAG;

            // 异步发送
            rocketMQTemplate.asyncSend(destination, MessageBuilder.withPayload(message).build(),
                    new org.apache.rocketmq.client.producer.SendCallback() {
                        @Override
                        public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                            log.info("【异步发送购物车同步消息】成功, messageId={}", message.getMessageId());
                        }

                        @Override
                        public void onException(Throwable e) {
                            log.error("【异步发送购物车同步消息】失败, messageId={}", message.getMessageId(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("【异步发送购物车同步消息】异常, userId={}, skuId={}",
                    message.getUserId(), message.getSkuId(), e);
        }
    }
}
