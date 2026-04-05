package com.nexusmall.gateway;

import com.nexusmall.common.config.KafkaLoggingProperties;
import com.nexusmall.common.config.KafkaLoggingConfig;
import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Gateway 服务启动类
 */
@SpringBootApplication // 标记为 Spring Boot 应用，启用自动配置和组件扫描
@EnableDiscoveryClient // 启用服务发现，向 Nacos 注册服务
@EnableConfigurationProperties(KafkaLoggingProperties.class) // 启用 Kafka 日志配置属性绑定
@Import(RedisConfig.class)
@ComponentScan(basePackageClasses = {NexusmallGatewayApplication.class, RedisUtils.class})
public class NexusmallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallGatewayApplication.class, args);
    }
}
