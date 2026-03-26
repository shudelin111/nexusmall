package com.nexusmall.behavior;

import com.nexusmall.common.config.GlobalFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

/**
 * 用户行为日志服务启动类
 * 
 * @author shudl
 * @since 2026-03-25
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.nexusmall.behavior.dao")
@Import({GlobalFeignConfig.class})
public class NexusmallBehaviorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallBehaviorApplication.class, args);
        System.out.println("========================================");
        System.out.println("NexusMall Behavior Service Started!");
        System.out.println("用户行为日志服务已启动");
        System.out.println("========================================");
    }
}
