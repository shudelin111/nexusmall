package com.nexusmall.payment;

import com.nexusmall.common.aspect.SentinelBlockExceptionHandler;
import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.config.SeataFeignConfig;
import com.nexusmall.common.filter.SeataXidFilter;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NexusMall 支付服务启动类
 * <p>
 * 职责：
 * - 支付单管理（创建、查询、状态更新）
 * - 支付渠道集成（支付宝、微信支付、银联等）
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
@EnableConfigurationProperties(KafkaLoggingProperties.class)
@Import({SeataFeignConfig.class, GlobalFeignConfig.class})
@ComponentScan(basePackageClasses = {
        NexusmallPaymentApplication.class,
        RedisUtils.class,
        SeataXidFilter.class,
        SentinelBlockExceptionHandler.class,
        KafkaLoggingConfig.class
})
public class NexusmallPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallPaymentApplication.class, args);
    }
}
