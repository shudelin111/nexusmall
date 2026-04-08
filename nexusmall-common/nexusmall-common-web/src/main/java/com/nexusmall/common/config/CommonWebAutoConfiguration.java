package com.nexusmall.common.config;

import com.nexusmall.common.filter.SeataXidFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Common Web 模块自动配置入口
 * <p>
 * 负责注册全局通用的 Filter、Aspect 和 Utility 组件。
 * 注意：拦截器（HandlerInterceptor）不在此自动注册，由各业务模块 WebConfig 根据路径需求手动注册。
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
@ComponentScan(basePackages = {
        "com.nexusmall.common.aspect",
        "com.nexusmall.common.util"
})
public class CommonWebAutoConfiguration {

    /**
     * 注册 Seata XID 过滤器
     * <p>
     * 生产级实践：通过 FilterRegistrationBean 显式注册 Filter，
     * 可以精确控制其执行顺序和拦截路径，避免 @Component 扫描带来的不确定性。
     * </p>
     */
    @Bean
    public FilterRegistrationBean<SeataXidFilter> seataXidFilter() {
        FilterRegistrationBean<SeataXidFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SeataXidFilter());
        registration.addUrlPatterns("/*");
        registration.setName("seataXidFilter");
        registration.setOrder(1000); // 对应 Ordered.HIGHEST_PRECEDENCE + 1000
        return registration;
    }
}
