package com.nexusmall.common.config;

import com.nexusmall.common.feign.FeignApiVersionInterceptor;
import com.nexusmall.common.feign.FeignAuthInterceptor;
import feign.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Feign 和 RestTemplate 全局配置
 * 
 * <p>业界标准实践：使用 @ConditionalOnClass 实现条件化加载</p>
 * <p>只有当 spring-cloud-starter-openfeign 依赖存在时，才会加载此配置类</p>
 * 
 * @author shudl
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.cloud.openfeign.EnableFeignClients")
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
     * 配置 Feign 日志级别（根据环境动态调整）
     * <p>
     * 生产级实践：
     * - dev/test 环境：FULL（完整日志，便于调试）
     * - prod 环境：BASIC（基本日志，减少性能开销）
     * </p>
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        // 根据 Spring Profile 动态设置日志级别
        String activeProfile = System.getProperty("spring.profiles.active", 
                                  System.getenv("SPRING_PROFILES_ACTIVE"));
        
        if ("prod".equalsIgnoreCase(activeProfile)) {
            return Logger.Level.BASIC; // 生产环境使用 BASIC
        } else {
            return Logger.Level.FULL; // 开发/测试环境使用 FULL
        }
    }

    /**
     * 配置 Feign API 版本拦截器（全局通用）
     * <p>
     * 自动透传 X-API-Version Header 到下游服务
     * </p>
     */
    @Bean
    public FeignApiVersionInterceptor feignApiVersionInterceptor() {
        return new FeignApiVersionInterceptor();
    }

    /**
     * 配置 Feign 认证拦截器（全局通用）
     * <p>
     * 自动透传用户认证信息到下游服务
     * </p>
     */
    @Bean
    public FeignAuthInterceptor feignAuthInterceptor() {
        return new FeignAuthInterceptor();
    }
}
