package com.nexusmall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Product 服务启动?
 */
@SpringBootApplication // 标记?Spring Boot 应用，启用自动配置和组件扫描
@EnableDiscoveryClient // 启用服务发现，向 Nacos 注册服务
@EnableFeignClients // 启用 Feign 客户端，扫描并注?Feign 接口
public class NexusmallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallProductApplication.class, args);
    }

}
