package com.nexusmall.product.config;

import com.nexusmall.common.interceptor.ApiVersionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>
 * 注册全局拦截?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SeataHandlerInterceptor seataHandlerInterceptor;

    /**
     * 创建 API 版本拦截?Bean
     */
    @Bean
    public ApiVersionInterceptor apiVersionInterceptor() {
        return new ApiVersionInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Seata XID 拦截器（使用已注册的 Bean?
        registry.addInterceptor(seataHandlerInterceptor)
                .addPathPatterns("/**");
        
        // 注册 API 版本拦截?
        registry.addInterceptor(apiVersionInterceptor())
                .addPathPatterns("/**", "/brands/**", "/categories/**")  // 拦截所有业务接?
                .excludePathPatterns("/actuator/**", "/doc.html", "/swagger-resources/**", "/v3/api-docs/**");  // 排除监控和文?
    }
}
