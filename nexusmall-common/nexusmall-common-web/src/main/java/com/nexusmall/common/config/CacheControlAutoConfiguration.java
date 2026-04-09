package com.nexusmall.common.config;

import com.nexusmall.common.interceptor.CacheControlInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * HTTP 缓存控制自动配置
 * <p>
 * 业界标准（Spring Boot 官方推荐）：
 * - 通过实现 WebMvcConfigurer 接口注册拦截器
 * - 利用 Spring Boot 自动配置机制统一管理
 * - 所有依赖 common-web 的模块自动生效
 * - 避免每个模块重复配置
 * </p>
 * <p>
 * 参考文档：
 * - Spring Boot MVC Auto-configuration: https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.spring-mvc.auto-configuration
 * - WebMvcConfigurer Best Practices: https://spring.io/guides/gs/rest-service/
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Configuration
public class CacheControlAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private CacheControlInterceptor cacheControlInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 HTTP 缓存控制拦截器
        registry.addInterceptor(cacheControlInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        // 排除监控端点
                        "/actuator/**",
                        // 排除错误页面
                        "/error",
                        // 排除 Swagger 文档
                        "/doc.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**"
                );
    }

    /**
     * 创建缓存控制拦截器 Bean
     * <p>
     * 使用 @Bean + @ConditionalOnMissingBean 确保：
     * 1. 全局只有一个实例
     * 2. 允许用户自定义覆盖
     * </p>
     *
     * @return CacheControlInterceptor
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
    public CacheControlInterceptor cacheControlInterceptor() {
        return new CacheControlInterceptor();
    }
}
