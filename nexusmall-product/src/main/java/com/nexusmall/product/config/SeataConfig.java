package com.nexusmall.product.config;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.springframework.context.annotation.Configuration;

/**
 * Seata 配置类
 * 启用自动数据源代理，支持分布式事务
 */
@Configuration
@EnableAutoDataSourceProxy(dataSourceProxyMode = "AT")
public class SeataConfig {
}
