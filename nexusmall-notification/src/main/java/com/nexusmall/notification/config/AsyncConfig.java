package com.nexusmall.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * <p>
 * 业界标准（Async Thread Pool）：
 * - 用于批量发送通知的异步处理
 * - 隔离业务线程池，避免影响主业务流程
 * - 支持优雅关闭和拒绝策略
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 通知发送线程池
     * <p>
     * 核心参数说明：
     * - corePoolSize: 核心线程数，保持活跃
     * - maxPoolSize: 最大线程数，高峰期扩容
     * - queueCapacity: 队列容量，缓冲任务
     * - keepAliveSeconds: 非核心线程空闲存活时间
     * - rejectedExecutionHandler: 拒绝策略（CallerRunsPolicy - 由调用线程执行）
     * </p>
     */
    @Bean("notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU 核心数 * 2
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        
        // 最大线程数：核心线程数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 4);
        
        // 队列容量：1000
        executor.setQueueCapacity(1000);
        
        // 线程名前缀
        executor.setThreadNamePrefix("notification-async-");
        
        // 非核心线程空闲存活时间：60秒
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：由调用线程执行（降级策略）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：60秒
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化
        executor.initialize();
        
        log.info("通知异步线程池初始化完成，corePoolSize: {}, maxPoolSize: {}, queueCapacity: {}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}
