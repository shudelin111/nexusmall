package com.nexusmall.cart;

import com.nexusmall.common.aspect.SentinelBlockExceptionHandler;
import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.config.RedissonConfig;
import com.nexusmall.common.config.SeataDataSourceConfig;
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
@EnableConfigurationProperties(KafkaLoggingProperties.class)
@Import({RedisConfig.class, RedissonConfig.class, SeataDataSourceConfig.class, SeataFeignConfig.class, GlobalFeignConfig.class})
@ComponentScan(basePackageClasses = {
        NexusmallCartApplication.class,
        RedisUtils.class,
        SeataXidFilter.class,
        SentinelBlockExceptionHandler.class,
        KafkaLoggingConfig.class
})
public class NexusmallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallCartApplication.class, args);
    }
}
