package com.nexusmall.order;

import com.nexusmall.common.aspect.SentinelBlockExceptionHandler;
import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.config.RedissonConfig;
import com.nexusmall.common.config.SeataDataSourceConfig;
import com.nexusmall.common.config.SeataFeignConfig;
import com.nexusmall.common.filter.SeataXidFilter;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Order 服务启动类
 */
@SpringBootApplication // 标记为 Spring Boot 应用，启用自动配置和组件扫描
@EnableDiscoveryClient // 启用服务发现，向 Nacos 注册服务
@EnableFeignClients // 启用 Feign 客户端，扫描并注册 Feign 接口
@Import({RedisConfig.class, RedissonConfig.class, SeataDataSourceConfig.class, SeataFeignConfig.class, GlobalFeignConfig.class}) // 导入配置类，加载 Redis、Redisson、Seata 和 Feign 全局配置
@ComponentScan(basePackageClasses = {
        NexusmallOrderApplication.class, 
        RedisUtils.class, 
        SeataXidFilter.class,
        SentinelBlockExceptionHandler.class, // 扫描 Sentinel 全局异常处理器
        KafkaLoggingConfig.class // 扫描 Kafka 日志配置（延迟初始化）
})
public class NexusmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallOrderApplication.class, args);
    }
}
