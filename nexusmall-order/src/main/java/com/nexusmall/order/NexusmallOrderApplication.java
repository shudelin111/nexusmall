package com.nexusmall.order;

import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.config.SeataFeignConfig;
import com.nexusmall.common.filter.SeataXidFilter;
import com.nexusmall.common.util.RedisUtils;
import com.nexusmall.order.config.SeataConfig;
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
@Import({RedisConfig.class, SeataConfig.class, SeataFeignConfig.class}) // 导入配置类，加载 Redis、Seata 和 Feign 配置
@ComponentScan(basePackageClasses = {
        NexusmallOrderApplication.class, 
        RedisUtils.class, 
        SeataXidFilter.class,
        com.nexusmall.common.aspect.SentinelBlockExceptionHandler.class // 扫描 Sentinel 全局异常处理器
})
public class NexusmallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallOrderApplication.class, args);
    }
}
