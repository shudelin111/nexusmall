package com.nexusmall.behavior.service;

import com.nexusmall.behavior.entity.UserBehaviorEsLog;

/**
 * 用户行为日志 Elasticsearch Service 接口
 * 
 * @author NexusMall
 * @since 2026-03-25
 */
public interface UserBehaviorEsService {

    /**
     * 保存用户行为日志到 Elasticsearch
     * 
     * @param esLog 行为日志
     * @return 是否成功
     */
    boolean save(UserBehaviorEsLog esLog);

    /**
     * 从 UserBehaviorVO 转换并保存到 ES
     * 
     * @param userId 用户 ID
     * @param behaviorType 行为类型
     * @param objectId 业务对象 ID
     * @param objectType 业务对象类型
     * @param extraData 额外信息
     * @param ipAddress IP 地址
     * @param userAgent User-Agent
     * @param occurTime 行为发生时间
     * @return 是否成功
     */
    boolean saveFromVO(Long userId, String behaviorType, Long objectId,
                       String objectType, String extraData, String ipAddress,
                       String userAgent, java.time.LocalDateTime occurTime);

}
