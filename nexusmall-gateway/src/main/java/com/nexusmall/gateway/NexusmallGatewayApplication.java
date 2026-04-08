package com.nexusmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Gateway 服务启动类
 * <p>
 * 业务职责：
 * - API 网关，统一请求入口（所有客户端请求的入口）
 * - 全局过滤器链（Token 验证、权限校验、限流）
 * - 请求路由到下游微服务（动态路由配置）
 * - 跨域配置、异常处理
 * <p>
 * 技术特性：
 * - 基于 Spring Cloud Gateway（WebFlux，响应式编程）
 * - 全局 Token 验证过滤器（JWT 令牌校验）
 * - Sentinel 限流保护（流量控制）
 * - Redis 存储会话信息（Token 黑名单、用户会话）
 * - Kafka 日志收集（异步日志输出）
 * <p>
 * 路由规则：
 * - /api/auth/** → nexusmall-auth（认证服务）
 * - /api/order/** → nexusmall-order（订单服务）
 * - /api/product/** → nexusmall-product（商品服务）
 * - /api/member/** → nexusmall-member（会员服务）
 *
 * @author shudl
 * @since 2026-04-07
 */
@SpringBootApplication // 标记为 Spring Boot 应用，启用自动配置和组件扫描
@EnableDiscoveryClient // 启用服务发现，向 Nacos 注册服务（动态路由依赖）
public class NexusmallGatewayApplication {

    public static void main(String[] args) {
        // 启动网关服务，加载路由配置和全局过滤器链
        SpringApplication.run(NexusmallGatewayApplication.class, args);
    }
}
