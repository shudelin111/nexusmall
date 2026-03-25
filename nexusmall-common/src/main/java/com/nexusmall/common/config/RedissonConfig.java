package com.nexusmall.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置
 * 
 * @author shudl
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host:10.10.1.1}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:123456}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * 创建 RedissonClient 实例
     * 用于实现分布式锁、限流等功能
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 使用单节点模式（生产环境可使用集群或哨兵模式）
        String address = String.format("redis://%s:%d", redisHost, redisPort);
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisDatabase)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                .setConnectionMinimumIdleSize(8)      // 最小连接数
                .setConnectionPoolSize(32)            // 连接池大小
                .setIdleConnectionTimeout(10000)      // 空闲连接超时时间
                .setConnectTimeout(10000)             // 连接超时时间
                .setTimeout(3000);                    // 命令等待超时时间
        
        return Redisson.create(config);
    }
}
