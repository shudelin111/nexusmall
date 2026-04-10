package com.nexusmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * NexusMall 购物车服务启动类
 * <p>
 * 职责：
 * - 购物车管理（添加/删除/修改数量/查询）
 * - Redis Hash 主存储（高性能读写）
 * - MySQL 异步持久化（数据兜底）
 * - 商品快照机制（防止商家改价导致体验问题）
 * - 匿名购物车合并（未登录→登录后自动合并）
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NexusmallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallCartApplication.class, args);
    }
}
