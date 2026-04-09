package com.nexusmall.common.config;

import com.nexusmall.common.aspect.AuditLogAspect;
import com.nexusmall.common.aspect.DistributedLockAspect;
import com.nexusmall.common.aspect.IdempotentAspect;
import com.nexusmall.common.aspect.SentinelBlockExceptionHandler;
import com.nexusmall.common.exception.GlobalExceptionHandler;
import com.nexusmall.common.filter.SeataXidFilter;
import com.nexusmall.common.interceptor.CacheControlInterceptor;
import com.nexusmall.common.util.DistributedLockUtil;
import com.nexusmall.common.util.RedisUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Common Web 模块自动配置入口
 * <p>
 * 生产级实践：
 * 1. 所有组件统一通过@Bean方法注册，不使用@Component自动扫描
 * 2. 使用构造器注入，便于单元测试和依赖管理
 * 3. 条件化加载，避免不必要的Bean创建
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
public class CommonWebAutoConfiguration {

    /**
     * 注册全局异常处理器
     * <p>
     * 生产级实践：
     * 1. 统一处理所有Controller抛出的异常
     * 2. 返回标准化的JSON响应
     * 3. 生产环境脱敏，开发环境返回详细信息
     * </p>
     *
     * @return GlobalExceptionHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

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

    /**
     * 注册幂等性切面
     * <p>
     * 生产级实践：基于Redis实现分布式幂等性控制，防止重复提交
     * </p>
     *
     * @param redisTemplate Redis模板
     * @return IdempotentAspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(StringRedisTemplate.class)
    @ConditionalOnProperty(name = "idempotent.enabled", havingValue = "true", matchIfMissing = true)
    public IdempotentAspect idempotentAspect(StringRedisTemplate redisTemplate) {
        return new IdempotentAspect(redisTemplate);
    }

    /**
     * 注册分布式锁切面
     * <p>
     * 生产级实践：基于Redisson实现分布式锁，防止并发冲突
     * </p>
     *
     * @param redissonClient Redisson客户端
     * @return DistributedLockAspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    public DistributedLockAspect distributedLockAspect(org.redisson.api.RedissonClient redissonClient) {
        return new DistributedLockAspect(redissonClient);
    }

    /**
     * 注册Sentinel流控异常处理器
     * <p>
     * 生产级实践：统一处理Sentinel限流、熔断、降级等异常
     * </p>
     *
     * @return SentinelBlockExceptionHandler
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler")
    public SentinelBlockExceptionHandler sentinelBlockExceptionHandler() {
        return new SentinelBlockExceptionHandler();
    }

    /**
     * 注册审计日志切面
     * <p>
     * 生产级实践：记录关键操作的审计日志，包括操作人、IP、耗时等
     * </p>
     *
     * @return AuditLogAspect
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "javax.servlet.http.HttpServletRequest")
    @ConditionalOnProperty(name = "audit.log.enabled", havingValue = "true", matchIfMissing = true)
    public AuditLogAspect auditLogAspect() {
        return new AuditLogAspect();
    }

    /**
     * 注册HTTP缓存控制拦截器
     * <p>
     * 生产级实践：
     * 1. 为静态资源添加强缓存（7天）
     * 2. 为API响应添加协商缓存或禁止缓存
     * 3. 支持ETag验证
     * </p>
     *
     * @return CacheControlInterceptor
     */
    @Bean
    @ConditionalOnMissingBean
    public CacheControlInterceptor cacheControlInterceptor() {
        return new CacheControlInterceptor();
    }

    /**
     * 注册HTTP缓存控制拦截器配置
     * <p>
     * 生产级实践：
     * 1. 自动注册到所有业务模块，无需手动配置
     * 2. 排除健康检查和错误页面
     * </p>
     *
     * @param cacheControlInterceptor 缓存控制拦截器
     * @return WebMvcConfigurer
     */
    @Bean
    @ConditionalOnMissingBean(name = "cacheControlWebMvcConfigurer")
    public WebMvcConfigurer cacheControlWebMvcConfigurer(CacheControlInterceptor cacheControlInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(cacheControlInterceptor)
                        .addPathPatterns("/**")  // 拦截所有请求
                        .excludePathPatterns(
                                "/actuator/**",  // 排除健康检查
                                "/error"         // 排除错误页面
                        );
            }
        };
    }

    /**
     * 注册分布式锁工具类
     * <p>
     * 生产级实践：提供便捷的分布式锁操作API
     * </p>
     *
     * @param redissonClient Redisson客户端
     * @return DistributedLockUtil
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    public DistributedLockUtil distributedLockUtil(org.redisson.api.RedissonClient redissonClient) {
        return new DistributedLockUtil(redissonClient);
    }

    /**
     * 注册Redis工具类
     * <p>
     * 生产级实践：封装常用Redis操作，简化开发
     * </p>
     *
     * @param redisTemplate Redis模板
     * @return RedisUtils
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    public RedisUtils redisUtils(RedisTemplate<String, Object> redisTemplate) {
        return new RedisUtils(redisTemplate);
    }
}
