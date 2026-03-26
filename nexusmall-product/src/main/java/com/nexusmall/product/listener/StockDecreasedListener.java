package com.nexusmall.product.listener;

import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.product.service.RocketMQProducer.StockDecreasedMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 库存扣减消息消费者（监听普通消息）
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = MQConstants.Stock.TOPIC,
    selectorExpression = MQConstants.Stock.DECREASED_TAG,
    consumerGroup = MQConstants.Stock.CONSUMER_GROUP
)
public class StockDecreasedListener implements RocketMQListener<StockDecreasedMessage> {

    @Override
    public void onMessage(StockDecreasedMessage message) {
        log.info("收到库存扣减消息，skuId: {}, count: {}", message.getSkuId(), message.getCount());
        
        try {
            // TODO: 这里可以更新 Redis 缓存
            // 例如：redisTemplate.opsForValue().set("stock:" + message.getSkuId(), newStock);
            
            log.info("库存缓存更新成功，skuId: {}", message.getSkuId());
        } catch (Exception e) {
            log.error("更新库存缓存失败，skuId: {}", message.getSkuId(), e);
        }
    }

}
