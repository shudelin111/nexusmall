package com.nexusmall.gateway;

import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Gateway 服务启动类
 * <p>
 * 业务职责：
 * - API 网关，统一请求入口
 * - 全局过滤器链（Token 验证、权限校验、限流）
 * - 请求路由到下游微服务
 * - 跨域配置、异常处理
 * <p>
 * 技术特性：
 * - 基于 Spring Cloud Gateway（WebFlux）
 * - 全局 Token 验证过滤器
 * - Sentinel 限流保护
 * - Redis 存储会话信息
 * - Kafka 日志收集
 *
 * @author shudl
 * @since 2026-04-07
 */
@SpringBootApplication // 标记为 Spring Boot 应用，启用自动配置和组件扫描
@EnableDiscoveryClient // 启用服务发现，向 Nacos 注册服务
@EnableConfigurationProperties(KafkaLoggingProperties.class) // 启用 Kafka 日志配置属性绑定
@Import(RedisConfig.class) // 导入 Redis 配置
@ComponentScan(basePackageClasses = {
        NexusmallGatewayApplication.class,
        RedisUtils.class
})
public class NexusmallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallGatewayApplication.class, args);
    }
}
