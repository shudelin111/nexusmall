package com.nexusmall.order.config;

import feign.Logger;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Feign 和 RestTemplate 配置（本服务特定配置）
 * Seata XID 传递已由 nexusmall-common 的 SeataFeignConfig 全局处理
 */
@Configuration
public class FeignConfig {

    /**
     * 配置支持负载均衡的 RestTemplate
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
