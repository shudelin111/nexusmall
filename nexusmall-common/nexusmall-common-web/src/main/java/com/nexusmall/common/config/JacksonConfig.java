package com.nexusmall.common.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nexusmall.common.annotation.Sensitive;
import com.nexusmall.common.util.DesensitizationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.lang.annotation.Annotation;
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
@Slf4j
@Configuration
@ConditionalOnClass(ObjectMapper.class)
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
     * - 注册敏感数据序列化器（自动脱敏）
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
        
        // 注册敏感数据序列化器（@Sensitive注解自动脱敏）
        SimpleModule sensitiveModule = new SimpleModule("SensitiveDataModule");
        sensitiveModule.addSerializer(String.class, new SensitiveDataSerializer());
        objectMapper.registerModule(sensitiveModule);
        
        return objectMapper;
    }

    /**
     * 敏感数据 Jackson 序列化器（私有内部类）
     * <p>
     * 生产级实践：
     * 1. 作为内部类封装在 JacksonConfig 中，不对外暴露
     * 2. JSON 序列化时自动检测 @Sensitive 注解并脱敏
     * 3. 支持字段和方法级别的注解
     * </p>
     */
    private static class SensitiveDataSerializer extends StdSerializer<String> implements ContextualSerializer {

        private static final long serialVersionUID = 1L;

        /**
         * 脱敏类型（从注解中获取）
         */
        private DesensitizationUtils.SensitiveType sensitiveType;

        public SensitiveDataSerializer() {
            super(String.class);
        }

        public SensitiveDataSerializer(DesensitizationUtils.SensitiveType sensitiveType) {
            super(String.class);
            this.sensitiveType = sensitiveType;
        }

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            // 如果有脱敏类型，执行脱敏
            if (sensitiveType != null) {
                String desensitizedValue = DesensitizationUtils.desensitize(value, sensitiveType);
                gen.writeString(desensitizedValue);
            } else {
                // 没有脱敏类型，原样输出
                gen.writeString(value);
            }
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            // 获取字段上的 @Sensitive 注解
            Sensitive sensitive = getAnnotation(property, Sensitive.class);
            
            if (sensitive != null) {
                // 返回带脱敏类型的序列化器
                return new SensitiveDataSerializer(sensitive.type());
            }
            
            // 没有注解，返回默认序列化器
            return this;
        }

        /**
         * 获取注解（支持字段和方法级别）
         */
        private <A extends Annotation> A getAnnotation(BeanProperty property, Class<A> annotationType) {
            // 先尝试从字段获取
            A annotation = property.getAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
            
            // 再尝试从 getter 方法获取
            if (property.getMember() != null) {
                annotation = property.getMember().getAnnotation(annotationType);
            }
            
            return annotation;
        }
    }
}
