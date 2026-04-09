package com.nexusmall.common.config;

import com.nexusmall.common.filter.XssFilter;
import org.jsoup.Jsoup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

/**
 * XSS过滤器配置
 * <p>
 * 生产级实践：注册XSS过滤器到Spring容器，对所有请求进行XSS防护
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Configuration
@ConditionalOnClass({Filter.class, Jsoup.class})
@ConditionalOnProperty(name = "security.xss.enabled", havingValue = "true", matchIfMissing = true)
public class XssFilterConfig {

    /**
     * 注册XSS过滤器
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilterRegistration() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");  // 拦截所有请求
        registration.setName("xssFilter");
        registration.setOrder(1);  // 最高优先级
        
        return registration;
    }
}
