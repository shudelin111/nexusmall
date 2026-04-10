package com.nexusmall.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置
 * <p>
 * 生产级标准配置：
 * - 核心线程数：CPU 核心数 * 2
 * - 最大线程数：CPU 核心数 * 4
 * - 队列容量：1000
 * - 线程名前缀：async-task-
 * - 拒绝策略：CallerRunsPolicy（由调用线程执行）
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
@Configuration
@EnableAsync
@ConditionalOnClass(name = "org.springframework.scheduling.annotation.EnableAsync")
public class AsyncConfig {

    /**
     * 配置异步任务线程池
     * <p>
     * 用于 @Async 注解的方法
     * </p>
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 获取 CPU 核心数
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        // 核心线程数：CPU 核心数 * 2
        executor.setCorePoolSize(cpuCores * 2);
        
        // 最大线程数：CPU 核心数 * 4
        executor.setMaxPoolSize(cpuCores * 4);
        
        // 队列容量：1000
        executor.setQueueCapacity(1000);
        
        // 线程名前缀
        executor.setThreadNamePrefix("async-task-");
        
        // 线程空闲时间：60 秒
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：由调用线程执行（避免任务丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务结束后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：60 秒
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        return executor;
    }
}
