package com.nexusmall.behavior.listener;

import cn.hutool.json.JSONUtil;
import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.common.enums.UserBehaviorType;
import com.nexusmall.common.vo.UserBehaviorVO;
import com.nexusmall.behavior.service.UserBehaviorEsService;
import com.nexusmall.behavior.service.UserBehaviorKafkaService;
import com.nexusmall.behavior.service.UserBehaviorLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 用户行为消息监听器
 * 
 * 💡 用途：
 * 1. 监听 RocketMQ 中的用户行为消息
 * 2. 根据行为类型进行三路分发：
 *    - 关键行为 → MySQL + Kafka + ES
 *    - 一般行为 → Kafka + ES
 * 
 * @author shudl
 * @since 2026-03-25
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = MQConstants.UserBehavior.TOPIC,
    selectorExpression = "*",
    consumerGroup = MQConstants.UserBehavior.CONSUMER_GROUP
)
public class UserBehaviorListener implements RocketMQListener<String> {

    @Autowired
    private UserBehaviorLogService userBehaviorLogService;

    @Autowired
    private UserBehaviorEsService userBehaviorEsService;

    @Autowired
    private UserBehaviorKafkaService userBehaviorKafkaService;

    /**
     * 是否启用 Elasticsearch 存储
     */
    @Value("${user.behavior.es.enabled}")
    private Boolean esEnabled;

    /**
     * 是否启用 Kafka 发送
     */
    @Value("${user.behavior.kafka.enabled}")
    private Boolean kafkaEnabled;

    /**
     * 需要保存到数据库的关键行为类型
     */
    private static final List<String> CRITICAL_BEHAVIORS = Arrays.asList(
        UserBehaviorType.PLACE_ORDER.getCode(),
        UserBehaviorType.ADD_TO_CART.getCode()
    );

    @Override
    public void onMessage(String messageJson) {
        try {
            log.info("【收到用户行为消息】message: {}", messageJson);
            
            // 解析消息
            UserBehaviorVO behaviorVO = JSONUtil.toBean(messageJson, UserBehaviorVO.class);
            
            // 处理用户行为
            handleUserBehavior(behaviorVO);
            
        } catch (Exception e) {
            log.error("【处理用户行为消息失败】message: {}", messageJson, e);
            // 不抛异常，让 RocketMQ 自动重试
        }
    }

    /**
     * 处理用户行为
     */
    private void handleUserBehavior(UserBehaviorVO behaviorVO) {
        String behaviorType = behaviorVO.getBehaviorType();
        
        // 判断是否为关键行为
        if (isCriticalBehavior(behaviorType)) {
            // 关键行为：保存到 MySQL + Kafka + ES
            handleCriticalBehavior(behaviorVO);
        } else {
            // 一般行为：发送到 Kafka + ES
            handleNormalBehavior(behaviorVO);
        }
    }

    /**
     * 处理关键行为（三路分发）
     */
    private void handleCriticalBehavior(UserBehaviorVO behaviorVO) {
        log.info("【关键行为处理】userId: {}, behaviorType: {}", 
                behaviorVO.getUserId(), behaviorVO.getBehaviorType());
        
        // 1. 保存到 MySQL
        userBehaviorLogService.recordBehavior(
            behaviorVO.getUserId(),
            behaviorVO.getBehaviorType(),
            behaviorVO.getObjectId(),
            behaviorVO.getObjectType(),
            behaviorVO.getExtraData(),
            behaviorVO.getIpAddress(),
            behaviorVO.getUserAgent(),
            behaviorVO.getOccurTime()
        );
        
        // 2. 发送到 Kafka
        if (kafkaEnabled) {
            userBehaviorKafkaService.sendToKafka(
                behaviorVO.getUserId(),
                behaviorVO.getBehaviorType(),
                behaviorVO.getObjectId(),
                behaviorVO.getObjectType(),
                behaviorVO.getExtraData(),
                behaviorVO.getIpAddress(),
                behaviorVO.getUserAgent(),
                behaviorVO.getOccurTime()
            );
        }
        
        // 3. 保存到 ES
        if (esEnabled) {
            userBehaviorEsService.saveFromVO(
                behaviorVO.getUserId(),
                behaviorVO.getBehaviorType(),
                behaviorVO.getObjectId(),
                behaviorVO.getObjectType(),
                behaviorVO.getExtraData(),
                behaviorVO.getIpAddress(),
                behaviorVO.getUserAgent(),
                behaviorVO.getOccurTime()
            );
        }
    }

    /**
     * 处理一般行为（两路分发）
     */
    private void handleNormalBehavior(UserBehaviorVO behaviorVO) {
        log.info("【一般行为处理】userId: {}, behaviorType: {}", 
                behaviorVO.getUserId(), behaviorVO.getBehaviorType());
        
        // 1. 发送到 Kafka
        if (kafkaEnabled) {
            userBehaviorKafkaService.sendToKafka(
                behaviorVO.getUserId(),
                behaviorVO.getBehaviorType(),
                behaviorVO.getObjectId(),
                behaviorVO.getObjectType(),
                behaviorVO.getExtraData(),
                behaviorVO.getIpAddress(),
                behaviorVO.getUserAgent(),
                behaviorVO.getOccurTime()
            );
        }
        
        // 2. 保存到 ES
        if (esEnabled) {
            userBehaviorEsService.saveFromVO(
                behaviorVO.getUserId(),
                behaviorVO.getBehaviorType(),
                behaviorVO.getObjectId(),
                behaviorVO.getObjectType(),
                behaviorVO.getExtraData(),
                behaviorVO.getIpAddress(),
                behaviorVO.getUserAgent(),
                behaviorVO.getOccurTime()
            );
        }
    }

    /**
     * 判断是否为关键行为
     */
    private boolean isCriticalBehavior(String behaviorType) {
        return CRITICAL_BEHAVIORS.contains(behaviorType);
    }
}
