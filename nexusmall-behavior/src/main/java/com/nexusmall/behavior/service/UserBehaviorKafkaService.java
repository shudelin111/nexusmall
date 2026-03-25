package com.nexusmall.behavior.service;

/**
 * 用户行为日志 Kafka Service 接口
 * 
 * @author NexusMall
 * @since 2026-03-25
 */
public interface UserBehaviorKafkaService {

    /**
     * 发送用户行为日志到 Kafka
     * 
     * @param userId 用户 ID
     * @param behaviorType 行为类型
     * @param objectId 业务对象 ID
     * @param objectType 业务对象类型
     * @param extraData 额外信息
     * @param ipAddress IP 地址
     * @param userAgent User-Agent
     * @param occurTime 行为发生时间
     */
    void sendToKafka(Long userId, String behaviorType, Long objectId,
                     String objectType, String extraData, String ipAddress,
                     String userAgent, java.time.LocalDateTime occurTime);

}
