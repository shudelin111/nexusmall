package com.nexusmall.inventory;

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
 * NexusMall 库存服务启动类
 * <p>
 * 主要功能：
 * - SKU库存管理（查询、扣减、回滚、确认）
 * - 库存流水记录与追踪
 * - 库存预警机制（低库存提醒）
 * - 分布式锁防超卖/乐观锁并发控制
 * - 库存数据一致性保障
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
        NexusmallInventoryApplication.class,
        RedisUtils.class,
        SeataXidFilter.class,
        SentinelBlockExceptionHandler.class,
        KafkaLoggingConfig.class
})
public class NexusmallInventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallInventoryApplication.class, args);
    }
}
