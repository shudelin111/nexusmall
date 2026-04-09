package com.nexusmall.member.infrastructure.messaging;

import com.nexusmall.member.infrastructure.persistence.dao.MemberMapper;
import com.nexusmall.member.domain.entity.Member;
import com.nexusmall.member.infrastructure.messaging.UserRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户注册事件监听�?
 * <p>
 * 业界标准（方案二：异步创建）�?
 * - 监听 Auth 服务发送的用户注册事件
 * - 自动为新用户创建会员档案
 * - 实现最终一致性，提升系统吞吐�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Component
@RocketMQMessageListener(
    topic = "USER_REGISTERED_TOPIC",
    consumerGroup = "member-service-consumer-group"
)
public class UserRegisteredListener implements RocketMQListener<UserRegisteredEvent> {

    @Autowired
    private MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onMessage(UserRegisteredEvent event) {
        log.info("收到用户注册事件，userId: {}, username: {}", event.getUserId(), event.getUsername());

        try {
            // 1. 检查是否已存在会员档案（幂等性处理）
            Member existingMember = memberMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Member>()
                    .eq(Member::getUserId, event.getUserId())
            );

            if (existingMember != null) {
                log.warn("会员档案已存在，跳过创建，userId: {}", event.getUserId());
                return;
            }

            // 2. 创建会员档案
            Member member = Member.builder()
                .userId(event.getUserId())
                .username(event.getUsername())
                .nickname(event.getUsername()) // 默认昵称为用户名
                .phone(event.getPhone())
                .email(event.getEmail())
                .gender(0) // 默认未知
                .memberLevelId(1L) // 默认普通会员等�?
                .growthPoint(0)
                .integration(0)
                .status(1)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

            memberMapper.insert(member);

            log.info("会员档案创建成功，userId: {}, memberId: {}", event.getUserId(), member.getId());

        } catch (Exception e) {
            log.error("创建会员档案失败，userId: {}", event.getUserId(), e);
            throw e; // 抛出异常触发 RocketMQ 重试机制
        }
    }
}
