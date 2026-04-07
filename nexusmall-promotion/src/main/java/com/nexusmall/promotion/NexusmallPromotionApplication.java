package com.nexusmall.promotion;

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
import com.nexusmall.promotion.config.BloomFilterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * NexusMall 营销服务启动类
 * <p>
 * 职责：
 * - 优惠券管理（发放/领取/使用/过期）
 * - 秒杀活动管理
 * - 满减/折扣活动
 * - 积分商城
 * - 拼团活动
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling  // 启用定时任务
@EnableConfigurationProperties(KafkaLoggingProperties.class)
@Import({RedisConfig.class, RedissonConfig.class, SeataDataSourceConfig.class, SeataFeignConfig.class, GlobalFeignConfig.class, BloomFilterConfig.class})
@ComponentScan(basePackageClasses = {
        NexusmallPromotionApplication.class,
        RedisUtils.class,
        SeataXidFilter.class,
        SentinelBlockExceptionHandler.class,
        KafkaLoggingConfig.class
})
public class NexusmallPromotionApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallPromotionApplication.class, args);
    }
}
