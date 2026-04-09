package com.nexusmall.common.util;

import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.exception.NexusmallException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 分布式锁工具类
 * <p>
 * 生产级实践：
 * 1. 通过@Configuration类中的@Bean方法注册，而非@Component自动扫描
 * 2. 使用构造器注入，便于单元测试
 * 3. 条件化加载，仅在Redisson存在时生效
 * </p>
 * 
 * @author shudl
 */
@ConditionalOnClass(RedissonClient.class)
public class DistributedLockUtil {

    private final RedissonClient redissonClient;

    /**
     * 构造器注入（生产级实践）
     *
     * @param redissonClient Redisson客户端
     */
    public DistributedLockUtil(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

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
                throw new NexusmallException(ResultCode.LOCK_FAILED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NexusmallException(ResultCode.LOCK_INTERRUPTED, e);
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
                throw new NexusmallException(ResultCode.LOCK_FAILED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NexusmallException(ResultCode.LOCK_INTERRUPTED, e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
