package com.nexusmall.common.config;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.springframework.context.annotation.Configuration;

/**
 * Seata 数据源代理配置（全局通用）
 * 启用自动数据源代理，支持分布式事务（AT 模式）
 * 
 * @author shudl
 */
@Configuration
@EnableAutoDataSourceProxy(dataSourceProxyMode = "AT")
public class SeataDataSourceConfig {
}
