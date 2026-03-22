package com.nexusmall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient // 启用服务发现
@EnableFeignClients // 启用Feign客户端
public class NexusmallAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexusmallAuthApplication.class, args);
	}

}
