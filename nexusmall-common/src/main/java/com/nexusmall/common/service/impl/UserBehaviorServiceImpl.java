package com.nexusmall.common.service.impl;

import cn.hutool.json.JSONUtil;
import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.common.enums.UserBehaviorType;
import com.nexusmall.common.service.UserBehaviorService;
import com.nexusmall.common.vo.UserBehaviorVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户行为日志服务实现类
 */
@Slf4j
@Service
public class UserBehaviorServiceImpl implements UserBehaviorService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void recordBehavior(Long userId, UserBehaviorType behaviorType, Long objectId, String objectType, String extraData) {
        // 构建用户行为 VO
        UserBehaviorVO behaviorVO = UserBehaviorVO.builder()
                .userId(userId)
                .behaviorType(behaviorType)
                .objectId(objectId)
                .objectType(objectType)
                .extraData(extraData)
                .occurTime(LocalDateTime.now())
                .build();
        
        recordBehavior(behaviorVO);
    }

    @Override
    public void recordBehavior(UserBehaviorVO behaviorVO) {
        log.info("记录用户行为，userId: {}, behaviorType: {}, objectId: {}", 
                behaviorVO.getUserId(), behaviorVO.getBehaviorType(), behaviorVO.getObjectId());
        
        try {
            // 设置默认时间
            if (behaviorVO.getOccurTime() == null) {
                behaviorVO.setOccurTime(LocalDateTime.now());
            }
            
            // 发送消息到 MQ
            String destination = MQConstants.USER_BEHAVIOR_TOPIC + ":" + MQConstants.USER_BEHAVIOR_TAG;
            rocketMQTemplate.convertAndSend(destination, MessageBuilder.withPayload(behaviorVO).build());
            
            log.info("用户行为消息发送成功，userId: {}, behaviorType: {}", 
                    behaviorVO.getUserId(), behaviorVO.getBehaviorType());
        } catch (Exception e) {
            log.error("发送用户行为消息失败，userId: {}, behaviorType: {}", 
                    behaviorVO.getUserId(), behaviorVO.getBehaviorType(), e);
            // 不抛异常，避免影响主流程
        }
    }

}
