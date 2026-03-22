package com.nexusmall.thirdparty;

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
@ComponentScan(basePackageClasses = {NexusmallThirdPartyApplication.class, RedisUtils.class})
public class NexusmallThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallThirdPartyApplication.class, args);
    }
}
