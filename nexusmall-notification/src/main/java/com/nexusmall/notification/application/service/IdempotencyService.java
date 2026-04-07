package com.nexusmall.notification.application.service;

/**
 * 事件幂等性服务接口
 * <p>
 * 业界标准（Idempotency Pattern）：
 * - 使用 Redis SETNX 实现分布式幂等性校验
 * - 防止消息重复消费导致的数据不一致
 * - 支持自动过期清理，避免内存泄漏
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface IdempotencyService {

    /**
     * 检查并标记事件已处理（原子操作）
     * <p>
     * 使用 Redis SETNX 实现原子性检查和设置
     * </p>
     *
     * @param eventId 事件ID
     * @param expireSeconds 过期时间（秒），默认7天
     * @return true-首次处理，false-重复事件
     */
    boolean tryProcess(String eventId, long expireSeconds);

    /**
     * 检查事件是否已处理
     *
     * @param eventId 事件ID
     * @return true-已处理，false-未处理
     */
    boolean isProcessed(String eventId);

    /**
     * 标记事件已处理
     *
     * @param eventId 事件ID
     * @param expireSeconds 过期时间（秒）
     */
    void markProcessed(String eventId, long expireSeconds);

    /**
     * 删除事件处理记录（用于测试或数据纠错）
     *
     * @param eventId 事件ID
     */
    void removeProcessed(String eventId);
}
