package com.nexusmall.auth;

import com.nexusmall.common.config.GlobalFeignConfig;
import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.config.RedissonConfig;
import com.nexusmall.common.config.SeataDataSourceConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 认证服务启动类
 * <p>
 * 负责用户认证、Token 生成与验证
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Import({RedisConfig.class, RedissonConfig.class, SeataDataSourceConfig.class, GlobalFeignConfig.class}) // 导入配置类，加载 Redis、Redisson、Seata 和 Feign 全局配置
@ComponentScan(basePackageClasses = {NexusmallAuthApplication.class, RedisUtils.class})
public class NexusmallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallAuthApplication.class, args);
    }
}
