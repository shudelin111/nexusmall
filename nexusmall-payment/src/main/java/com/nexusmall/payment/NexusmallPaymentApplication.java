package com.nexusmall.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NexusMall 支付服务启动类
 * <p>
 * 职责：
 * - 支付单管理（创建、查询、状态更新）
 * - 支付渠道集成（支付宝、微信支付、银联等)
 * - 支付回调处理
 * - 退款管理
 * - 对账功能
 * - 监听订单创建事件，自动创建支付单
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class NexusmallPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallPaymentApplication.class, args);
    }
}
