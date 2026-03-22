package com.nexusmall.product;

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
@ComponentScan(basePackageClasses = {NexusmallProductApplication.class, RedisUtils.class})
public class NexusmallProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexusmallProductApplication.class, args);
	}

}
