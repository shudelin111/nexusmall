package com.nexusmall.notification.application.service.impl;

import com.nexusmall.notification.application.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 事件幂等性服务实现
 * <p>
 * 业界标准（Redis SETNX + TTL）：
 * - 使用 Redis SETNX 实现原子性幂等性校验
 * - 设置过期时间自动清理，避免内存泄漏
 * - 支持分布式环境下的并发控制
 * - Key 格式：notification:event:processed:{eventId}
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "notification:event:processed:";
    private static final String KEY_VALUE = "1";
    private static final long DEFAULT_EXPIRE_SECONDS = 7 * 24 * 3600; // 7天

    @Override
    public boolean tryProcess(String eventId, long expireSeconds) {
        String key = buildKey(eventId);
        
        try {
            // SETNX 原子操作：如果 key 不存在则设置，返回 true；否则返回 false
            // 使用 setIfAbsent 保证原子性，避免并发场景下的重复处理
            Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, KEY_VALUE, expireSeconds, TimeUnit.SECONDS);
            
            boolean result = Boolean.TRUE.equals(success);
            
            if (result) {
                log.debug("事件首次处理，eventId: {}", eventId);
            } else {
                log.warn("事件重复消费，eventId: {}", eventId);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("幂等性校验失败，eventId: {}", eventId, e);
            // 降级策略：Redis 异常时允许继续处理，由数据库唯一索引兜底
            // 避免因为 Redis 故障导致整个消息处理流程中断
            return true;
        }
    }

    @Override
    public boolean isProcessed(String eventId) {
        String key = buildKey(eventId);
        
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("查询事件处理状态失败，eventId: {}", eventId, e);
            return false;
        }
    }

    @Override
    public void markProcessed(String eventId, long expireSeconds) {
        String key = buildKey(eventId);
        
        try {
            redisTemplate.opsForValue().set(key, KEY_VALUE, expireSeconds, TimeUnit.SECONDS);
            log.debug("标记事件已处理，eventId: {}", eventId);
        } catch (Exception e) {
            log.error("标记事件处理状态失败，eventId: {}", eventId, e);
        }
    }

    @Override
    public void removeProcessed(String eventId) {
        String key = buildKey(eventId);
        
        try {
            redisTemplate.delete(key);
            log.info("删除事件处理记录，eventId: {}", eventId);
        } catch (Exception e) {
            log.error("删除事件处理记录失败，eventId: {}", eventId, e);
        }
    }

    /**
     * 构建 Redis Key
     * <p>
     * Key 格式：notification:event:processed:{eventId}
     * 例如：notification:event:processed:user-reg-12345
     * </p>
     *
     * @param eventId 事件唯一标识
     * @return Redis Key
     */
    private String buildKey(String eventId) {
        return KEY_PREFIX + eventId;
    }

    /**
     * 使用默认过期时间检查并标记
     */
    public boolean tryProcess(String eventId) {
        return tryProcess(eventId, DEFAULT_EXPIRE_SECONDS);
    }
}
