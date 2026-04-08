package com.nexusmall.notification.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户注册领域事件
 * <p>
 * 业界标准（DDD Event Sourcing）：
 * - Auth 服务注册成功后发布此事件
 * - Notification 服务监听并发送欢迎消息
 * - 实现最终一致性（异步解耦）
 * - 符合 CQRS 模式的事件驱动架构
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 注册时间戳（毫秒）
     */
    private Long timestamp;

    /**
     * 事件ID（用于幂等性校验）
     */
    private String eventId;
}
