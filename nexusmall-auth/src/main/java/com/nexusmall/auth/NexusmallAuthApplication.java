package com.nexusmall.auth;

import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@Import(RedisConfig.class)
@ComponentScan(basePackageClasses = {NexusmallAuthApplication.class, RedisUtils.class})
public class NexusmallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallAuthApplication.class, args);
    }
}
