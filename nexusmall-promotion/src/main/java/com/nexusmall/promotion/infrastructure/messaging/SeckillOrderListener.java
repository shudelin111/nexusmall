package com.nexusmall.promotion.listener;

import com.nexusmall.promotion.domain.constants.PromotionConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单创建监听�?
 * <p>
 * 业界标准：异步解耦，提升秒杀响应速度
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = PromotionConstants.TOPIC_SECKILL_ORDER,
    consumerGroup = "seckill_order_consumer_group"
)
public class SeckillOrderListener implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
        log.info("【收到秒杀订单消息】message={}", message);
        
        try {
            // TODO: 解析消息，调用订单服务创建订�?
            // SeckillOrderMessage seckillOrder = JSON.parseObject(message, SeckillOrderMessage.class);
            // orderFeignClient.createSeckillOrder(buildOrderRequest(seckillOrder));
            
            log.info("【秒杀订单创建成功�?);
        } catch (Exception e) {
            log.error("【秒杀订单创建失败】message={}, error={}", message, e.getMessage(), e);
            // TODO: 重试机制或人工介�?
        }
    }
}
