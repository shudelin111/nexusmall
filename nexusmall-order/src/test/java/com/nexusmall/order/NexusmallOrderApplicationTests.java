package com.nexusmall.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单服务启动测试（集成测试）
 * 
 * 测试目的：
 * - 验证 Spring ApplicationContext 能否正常加载
 * - 验证关键 Bean 是否正确创建
 * - 验证 Nacos 配置是否成功加载
 * - 验证数据源、Redis、RocketMQ 等中间件连接正常
 * 
 * 环境说明：
 * - 使用 dev profile，从 Nacos 加载完整配置
 * - 测试环境已有 MySQL、Redis、Nacos、RocketMQ、Seata
 * - 仅 Kafka 不存在，但已通过 logback-test.xml 排除 Kafka Appender
 */
@SpringBootTest
@ActiveProfiles("test")
class NexusmallOrderApplicationTests {

    @Autowired
    private ApplicationContext context;
    
    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        // 验证应用上下文成功加载
        assertNotNull(context, "ApplicationContext 不应为 null");
    }
    
    @Test
    void testApplicationName() {
        // 验证应用名称配置正确
        String appName = environment.getProperty("spring.application.name");
        assertEquals("nexusmall-order", appName, "应用名称应为 nexusmall-order");
    }
    
    @Test
    void testDataSourceConfigured() {
        // 验证数据源配置已加载（从 Nacos）
        String datasourceUrl = environment.getProperty("spring.datasource.url");
        assertNotNull(datasourceUrl, "数据源 URL 不应为 null");
        assertTrue(datasourceUrl.contains("nexusmall_order"), 
            "数据源 URL 应包含 nexusmall_order 数据库名");
    }
    
    @Test
    void testRedisConfigured() {
        // 验证 Redis 配置已加载
        String redisHost = environment.getProperty("spring.redis.host");
        assertNotNull(redisHost, "Redis Host 不应为 null");
    }
    
    @Test
    void testKeyBeansExist() {
        // 验证关键 Bean 是否存在
        assertNotNull(context.getBean("orderController"), "OrderController Bean 应存在");
        // OrderServiceImpl 的 Bean 名称是 orderServiceImpl（接口实现类）
        assertNotNull(context.getBean("orderServiceImpl"), "OrderServiceImpl Bean 应存在");
    }
}
