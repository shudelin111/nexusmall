package com.nexusmall.product.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SeataHandlerInterceptor seataHandlerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Seata XID 拦截器
        registry.addInterceptor(seataHandlerInterceptor)
                .addPathPatterns("/**");
    }
}
