package com.nexusmall.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Order 服务启动类
 * <p>
 * 业务职责：
 * - 订单创建、取消、退款、状态流转
 * - 订单查询和列表管理
 * - 分布式事务管理（Seata）
 * - 库存、促销、支付模块的远程调用（Feign）
 * <p>
 * 技术特性：
 * - 支持分布式事务（@GlobalTransactional）
 * - Redis 缓存订单状态和购物车数据
 * - Sentinel 限流保护
 * - Kafka 日志收集
 * <p>
 * 依赖服务：
 * - Product 服务：商品信息查询
 * - Inventory 服务：库存扣减
 * - Promotion 服务：促销活动校验
 * - Payment 服务：支付状态回调
 *
 * @author shudl
 * @since 2026-04-07
 */
@SpringBootApplication // 标记为 Spring Boot 应用，启用自动配置和组件扫描
@EnableDiscoveryClient // 启用服务发现，向 Nacos 注册服务（服务注册与发现）
@EnableFeignClients // 启用 Feign 客户端，扫描并注册 Feign 接口（声明式 HTTP 客户端）
public class NexusmallOrderApplication {

    public static void main(String[] args) {
        // 启动订单服务，初始化 Spring 上下文及分布式事务环境
        SpringApplication.run(NexusmallOrderApplication.class, args);
    }
}
