package com.nexusmall.promotion.config;

import com.nexusmall.common.interceptor.ApiVersionInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 注册全局拦截�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public ApiVersionInterceptor apiVersionInterceptor() {
        return new ApiVersionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 API 版本拦截�?
        registry.addInterceptor(apiVersionInterceptor())
                .addPathPatterns("/**")  // 拦截所有业务接�?
                .excludePathPatterns("/actuator/**", "/doc.html", "/swagger-resources/**", "/v3/api-docs/**");  // 排除监控和文�?
    }
}
