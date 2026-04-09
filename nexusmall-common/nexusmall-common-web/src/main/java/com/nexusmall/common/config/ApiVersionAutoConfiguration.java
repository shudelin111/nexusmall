package com.nexusmall.common.config;

import com.nexusmall.common.interceptor.ApiVersionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API 版本拦截器自动配置
 * <p>
 * 业界标准：
 * - 通过 Spring Boot 自动配置机制统一管理
 * - 所有依赖 common-web 的模块自动生效
 * - 避免每个模块重复配置
 * - 支持自定义排除路径（通过配置文件）
 * </p>
 * <p>
 * 使用方式：
 * 1. 确保模块依赖 nexusmall-common-web
 * 2. 如需自定义排除路径，在 application.yml 中配置：
 *    nexusmall:
 *      api-version:
 *        exclude-paths:
 *          - /custom/path/**
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Configuration
public class ApiVersionAutoConfiguration implements WebMvcConfigurer {

    @Autowired
    private ApiVersionInterceptor apiVersionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 API 版本拦截器
        registry.addInterceptor(apiVersionInterceptor)
                .addPathPatterns("/**")  // 拦截所有业务接口
                .excludePathPatterns(
                        // 排除监控端点
                        "/actuator/**",
                        // 排除 Swagger 文档
                        "/doc.html",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        // 排除健康检查
                        "/health",
                        "/ready",
                        "/live"
                );
    }
}
