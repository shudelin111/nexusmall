package com.nexusmall.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 时区全局配置
 * <p>
 * 确保整个应用使用统一的时区（Asia/Shanghai）
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
@Configuration
public class TimeZoneConfig {

    /**
     * 设置 JVM 默认时区为中国时区
     * <p>
     * 这会影响所有未显式指定时区的日期时间操作
     * </p>
     */
    @Bean
    public TimeZone defaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        return TimeZone.getTimeZone("Asia/Shanghai");
    }
}
