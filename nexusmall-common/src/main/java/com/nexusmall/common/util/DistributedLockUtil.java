package com.nexusmall.common.util;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 分布式锁工具类
 * 
 * @author NexusMall
 */
@Component
public class DistributedLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 尝试获取锁并执行操作
     * 
     * @param lockKey 锁的键名
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean isLocked = false;
        try {
            // 尝试获取锁，最多等待 5 秒，锁定后自动释放时间为 30 秒
            isLocked = lock.tryLock(5, 30, TimeUnit.SECONDS);
            
            if (isLocked) {
                return action.get();
            } else {
                throw new RuntimeException("获取分布式锁失败：" + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁被中断", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 尝试获取锁并执行操作（无返回值）
     * 
     * @param lockKey 锁的键名
     * @param action 要执行的操作
     */
    public void executeWithLock(String lockKey, Runnable action) {
        executeWithLock(lockKey, () -> {
            action.run();
            return null;
        });
    }

    /**
     * 自定义参数的分布式锁操作
     * 
     * @param lockKey 锁的键名
     * @param waitTime 等待时间（秒）
     * @param leaseTime 锁持有时间（秒），-1 表示不自动释放
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> action) {
        RLock lock = redissonClient.getLock(lockKey);
        
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(waitTime, leaseTime, unit);
            
            if (isLocked) {
                return action.get();
            } else {
                throw new RuntimeException("获取分布式锁失败：" + lockKey);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取分布式锁被中断", e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
