package com.nexusmall.common.listener;

import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.common.vo.UserBehaviorVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 用户行为日志消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = MQConstants.USER_BEHAVIOR_TOPIC,
    selectorExpression = MQConstants.USER_BEHAVIOR_TAG,
    consumerGroup = MQConstants.USER_BEHAVIOR_CONSUMER_GROUP
)
public class UserBehaviorListener implements RocketMQListener<UserBehaviorVO> {

    @Override
    public void onMessage(UserBehaviorVO behaviorVO) {
        log.info("收到用户行为消息，userId: {}, behaviorType: {}, objectId: {}", 
                behaviorVO.getUserId(), behaviorVO.getBehaviorType(), behaviorVO.getObjectId());
        
        try {
            // TODO: 这里可以将用户行为日志存储到数据库
            // 例如：userBehaviorMapper.insert(behaviorVO);
            
            // TODO: 或者同步到 Elasticsearch 用于大数据分析
            
            log.info("用户行为处理成功，userId: {}, behaviorType: {}", 
                    behaviorVO.getUserId(), behaviorVO.getBehaviorType());
        } catch (Exception e) {
            log.error("处理用户行为失败，userId: {}, behaviorType: {}", 
                    behaviorVO.getUserId(), behaviorVO.getBehaviorType(), e);
        }
    }

}
