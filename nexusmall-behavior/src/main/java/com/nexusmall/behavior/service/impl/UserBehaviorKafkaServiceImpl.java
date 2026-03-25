package com.nexusmall.behavior.service.impl;

import cn.hutool.json.JSONUtil;
import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.behavior.service.UserBehaviorKafkaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户行为日志 Kafka Service 实现类
 * 
 * @author shudl
 * @since 2026-03-25
 */
@Slf4j
@Service
public class UserBehaviorKafkaServiceImpl implements UserBehaviorKafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Override
    public void sendToKafka(Long userId, String behaviorType, Long objectId,
                            String objectType, String extraData, String ipAddress,
                            String userAgent, LocalDateTime occurTime) {
        try {
            // 构建消息体
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("behaviorType", behaviorType);
            message.put("objectId", objectId);
            message.put("objectType", objectType);
            message.put("extraData", extraData);
            message.put("ipAddress", ipAddress);
            message.put("userAgent", userAgent);
            message.put("occurTime", occurTime != null ? occurTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            message.put("timestamp", System.currentTimeMillis());
            
            // 序列化为 JSON
            String messageJson = objectMapper.writeValueAsString(message);
            
            // 发送到 Kafka
            kafkaTemplate.send(MQConstants.USER_BEHAVIOR_KAFKA_TOPIC, messageJson);
            
            log.info("【用户行为发送至 Kafka 成功】userId: {}, behaviorType: {}", 
                    userId, behaviorType);
        } catch (Exception e) {
            log.error("【用户行为发送至 Kafka 异常】userId: {}, behaviorType: {}", 
                    userId, behaviorType, e);
        }
    }
}
