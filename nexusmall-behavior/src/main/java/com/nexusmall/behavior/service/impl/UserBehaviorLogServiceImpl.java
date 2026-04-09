package com.nexusmall.behavior.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.behavior.dao.UserBehaviorLogMapper;
import com.nexusmall.behavior.entity.UserBehaviorLog;
import com.nexusmall.behavior.service.UserBehaviorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户行为日志 Service 实现�? * 
 * @author shudl
 * @since 2026-03-25
 */
@Slf4j
@Service
public class UserBehaviorLogServiceImpl extends ServiceImpl<UserBehaviorLogMapper, UserBehaviorLog> 
        implements UserBehaviorLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recordBehavior(Long userId, String behaviorType, Long objectId, 
                                  String objectType, String extraData, String ipAddress, 
                                  String userAgent, LocalDateTime occurTime) {
        try {
            UserBehaviorLog logEntity = UserBehaviorLog.builder()
                    .userId(userId)
                    .behaviorType(behaviorType)
                    .objectId(objectId)
                    .objectType(objectType)
                    .extraData(extraData)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .occurTime(occurTime != null ? occurTime : LocalDateTime.now())
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
            
            boolean result = this.save(logEntity);
            
            if (result) {
                log.info("【用户行为保存至 MySQL 成功】userId: {}, behaviorType: {}, objectId: {}", 
                        userId, behaviorType, objectId);
            } else {
                log.error("【用户行为保存至 MySQL 失败】userId: {}, behaviorType: {}", 
                        userId, behaviorType);
            }
            
            return result;
        } catch (Exception e) {
            log.error("【用户行为保存至 MySQL 异常】userId: {}, behaviorType: {}", 
                    userId, behaviorType, e);
            throw e;
        }
    }
}
