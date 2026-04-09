package com.nexusmall.common.config;

import org.redisson.api.RedissonClient;
import org.redisson.codec.FstCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置
 * 仅在 classpath 中存在 RedissonClient 时生效
 * 
 * @author shudl
 */
@Configuration
@ConditionalOnClass(RedissonClient.class)
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * 创建 RedissonClient 实例
     * 用于实现分布式锁、限流等功能
     * 显式配置使用 FST 编解码器，避免与 Kryo 版本冲突
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 关键：显式设置使用 FST 编解码器
        // 这会在 Redisson 初始化时生效，避免其自动检测并使用 Kryo5Codec
        config.setCodec(new FstCodec());
        
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
        
        return org.redisson.Redisson.create(config);
    }
}
