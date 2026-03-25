package com.nexusmall.common.service;

import com.nexusmall.common.enums.UserBehaviorType;
import com.nexusmall.common.vo.UserBehaviorVO;

/**
 * 用户行为日志服务接口
 */
public interface UserBehaviorService {

    /**
     * 记录用户行为
     * 
     * @param userId 用户 ID
     * @param behaviorType 行为类型
     * @param objectId 业务对象 ID
     * @param objectType 业务对象类型
     * @param extraData 额外信息
     */
    void recordBehavior(Long userId, UserBehaviorType behaviorType, Long objectId, String objectType, String extraData);

    /**
     * 记录用户行为（完整参数）
     * 
     * @param behaviorVO 用户行为 VO
     */
    void recordBehavior(UserBehaviorVO behaviorVO);

}
