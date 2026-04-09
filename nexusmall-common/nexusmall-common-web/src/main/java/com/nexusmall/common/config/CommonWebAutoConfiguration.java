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
 * 注意：
 * 1. 拦截器（HandlerInterceptor）不在此自动注册，由各业务模块 WebConfig 根据路径需求手动注册。
 * 2. GlobalExceptionHandler 不在此扫描，由各业务模块自行定义。
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
     * 生产级实践：
     * 1. 通过 FilterRegistrationBean 显式注册 Filter，精确控制执行顺序和拦截路径
     * 2. 使用 @ConditionalOnClass 确保只有在引入 Seata 依赖时才注册此 Filter
     * 3. 避免在未使用 Seata 的模块中因缺少依赖导致启动失败
     * </p>
     */
    @Bean
    @ConditionalOnClass(name = "io.seata.core.context.RootContext")
    public FilterRegistrationBean<SeataXidFilter> seataXidFilter() {
        FilterRegistrationBean<SeataXidFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SeataXidFilter());
        registration.addUrlPatterns("/*");
        registration.setName("seataXidFilter");
        registration.setOrder(1000); // 对应 Ordered.HIGHEST_PRECEDENCE + 1000
        return registration;
    }
}
