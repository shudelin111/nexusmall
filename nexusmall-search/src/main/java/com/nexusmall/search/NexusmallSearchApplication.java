package com.nexusmall.search;

import com.nexusmall.common.aspect.SentinelBlockExceptionHandler;
import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * NexusMall 搜索服务启动类
 * <p>
 * 职责：
 * - 商品全文搜索（Elasticsearch）
 * - 搜索建议/自动补全
 * - 搜索历史记录
 * - 热门搜索词统计
 * - 搜索结果排序/过滤
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching  // 启用 Spring Cache
@EnableConfigurationProperties(KafkaLoggingProperties.class)
@Import({GlobalFeignConfig.class})
@ComponentScan(basePackageClasses = {
        NexusmallSearchApplication.class,
        RedisUtils.class,
        SentinelBlockExceptionHandler.class,
        KafkaLoggingConfig.class
})
public class NexusmallSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallSearchApplication.class, args);
    }
}
