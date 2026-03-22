package com.nexusmall.gateway;

import com.nexusmall.common.config.RedisConfig;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableDiscoveryClient
@Import(RedisConfig.class)
@ComponentScan(basePackageClasses = {NexusmallGatewayApplication.class, RedisUtils.class})
public class NexusmallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallGatewayApplication.class, args);
    }
}
