package com.nexusmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

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
public class NexusmallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallAuthApplication.class, args);
    }
}
