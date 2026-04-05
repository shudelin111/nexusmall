package com.nexusmall.auth;

import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.config.RedissonConfig;
import com.nexusmall.common.config.SeataDataSourceConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 认证服务启动类
 * <p>
 * 负责用户认证、Token 生成与验证
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@SpringBootApplication // 标记为 Spring Boot 应用，启用自动配置和组件扫描
@EnableDiscoveryClient // 启用服务发现，向 Nacos 注册服务
@EnableFeignClients // 启用 Feign 客户端，扫描并注册 Feign 接口
@EnableConfigurationProperties(KafkaLoggingProperties.class) // 启用 Kafka 日志配置属性绑定
@Import({RedisConfig.class, RedissonConfig.class, SeataDataSourceConfig.class, GlobalFeignConfig.class}) // 导入配置类，加载 Redis、Redisson、Seata 和 Feign 全局配置
@ComponentScan(basePackageClasses = {NexusmallAuthApplication.class, RedisUtils.class})
public class NexusmallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallAuthApplication.class, args);
    }
}
