package com.nexusmall.common.config;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Seata 数据源代理配置（全局通用）
 * 
 * <p>业界标准实践：使用 @ConditionalOnClass 实现条件化加载</p>
 * <p>只有当 seata-spring-boot-starter 依赖存在时，才会加载此配置类</p>
 * <p>启用自动数据源代理，支持分布式事务（AT 模式）</p>
 * 
 * @author shudl
 */
@Configuration
@ConditionalOnClass(name = "io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy")
@EnableAutoDataSourceProxy(dataSourceProxyMode = "AT")
public class SeataDataSourceConfig {
}
