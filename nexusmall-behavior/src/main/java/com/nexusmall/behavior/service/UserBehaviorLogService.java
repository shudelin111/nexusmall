package com.nexusmall.behavior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.behavior.entity.UserBehaviorLog;

/**
 * 用户行为日志 Service 接口
 * 
 * @author shudl
 * @since 2026-03-25
 */
public interface UserBehaviorLogService extends IService<UserBehaviorLog> {

    /**
     * 记录用户行为
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
    boolean recordBehavior(Long userId, String behaviorType, Long objectId, 
                          String objectType, String extraData, String ipAddress, 
                          String userAgent, java.time.LocalDateTime occurTime);

}
