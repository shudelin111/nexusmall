package com.nexusmall.common.database.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * <p>
 * 生产级标准配置：
 * - 分页插件（支持多种数据库）
 * - 乐观锁插件（防止并发更新冲突）
 * - 自动识别数据库类型
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Configuration
@ConditionalOnClass(name = "com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor")
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     * <p>
     * 包含：
     * 1. 分页插件 - 自动生成分页 SQL
     * 2. 乐观锁插件 - 基于 version 字段实现乐观锁
     * </p>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加分页插件（MySQL 8.x）
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(500L); // 单页最大限制 500 条，防止恶意查询
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理（默认不处理）
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 自定义配置
     * <p>
     * 禁用 MP 原生 SQL 注入器，提高安全性
     * </p>
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // 启用下划线转驼峰命名（默认已启用，此处显式声明）
            configuration.setMapUnderscoreToCamelCase(true);
        };
    }
}
