package com.nexusmall.common.config;

import feign.Logger;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Feign 和 RestTemplate 全局配置
 * 
 * @author shudl
 */
@Configuration
public class GlobalFeignConfig {

    /**
     * 配置支持负载均衡的 RestTemplate（全局通用）
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 配置 Feign 日志级别（全局通用）
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
