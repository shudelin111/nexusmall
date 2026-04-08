package com.nexusmall.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Jackson JSON 序列化全局配置
 * <p>
 * 统一配置 JSON 序列化行为，确保所有服务使用一致的序列化规则
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置全局 ObjectMapper
     * <p>
     * 生产级标准配置：
     * - 时区设置为 Asia/Shanghai
     * - 日期格式统一为 yyyy-MM-dd HH:mm:ss
     * - 忽略 null 字段（减少传输数据量）
     * - 禁用将日期写为时间戳
     * - 注册 JavaTimeModule 支持 Java 8 时间 API
     * </p>
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 设置时区为中国时区
        objectMapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        
        // 设置日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        
        // 忽略 null 字段（不序列化 null 值）
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // 禁用将日期写为时间戳（使用字符串格式）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 注册 JavaTimeModule 以支持 Java 8 时间 API（LocalDateTime 等）
        objectMapper.registerModule(new JavaTimeModule());
        
        return objectMapper;
    }
}
