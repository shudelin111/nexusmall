package com.nexusmall.product.application.service;

import com.nexusmall.common.constant.MQConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RocketMQ 消息生产�?
 */
@Slf4j
@Service
public class RocketMQProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 发送库存扣减成功消�?
     * 
     * @param skuId 商品 ID
     * @param count 扣减数量
     */
    public void sendStockDecreasedMessage(Long skuId, Integer count) {
        log.info("发送库存扣减消息，skuId: {}, count: {}", skuId, count);
        
        try {
            // 构建消息体（简单对象）
            StockDecreasedMessage message = new StockDecreasedMessage();
            message.setSkuId(skuId);
            message.setCount(count);
            
            // 发送到库存 Topic
            String destination = MQConstants.Stock.TOPIC + ":" + MQConstants.Stock.DECREASED_TAG;
            
            // 发送普通消�?
            rocketMQTemplate.convertAndSend(destination, message);
            
            log.info("库存扣减消息发送成功，skuId: {}, count: {}", skuId, count);
        } catch (Exception e) {
            log.error("发送库存扣减消息失败，skuId: {}, count: {}", skuId, count, e);
            // 这里不抛异常，避免影响主流程
        }
    }

    /**
     * 库存扣减消息实体
     */
    public static class StockDecreasedMessage {
        private Long skuId;
        private Integer count;

        public Long getSkuId() {
            return skuId;
        }

        public void setSkuId(Long skuId) {
            this.skuId = skuId;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

}
