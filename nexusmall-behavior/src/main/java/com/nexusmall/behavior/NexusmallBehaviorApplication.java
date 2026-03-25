package com.nexusmall.behavior;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户行为日志服务启动类
 * 
 * @author NexusMall
 * @since 2026-03-25
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.nexusmall.behavior.dao")
public class NexusmallBehaviorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallBehaviorApplication.class, args);
        System.out.println("========================================");
        System.out.println("NexusMall Behavior Service Started!");
        System.out.println("用户行为日志服务已启动");
        System.out.println("========================================");
    }
}
