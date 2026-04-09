package com.nexusmall.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.nexusmall.common.annotation.Sensitive;
import com.nexusmall.common.util.DesensitizationUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * 敏感数据Jackson序列化器
 * <p>
 * 生产级实践：JSON序列化时自动检测@Sensitive注解并脱敏
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
public class SensitiveDataSerializer extends StdSerializer<String> implements ContextualSerializer {

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
        // 获取字段上的@Sensitive注解
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
        
        // 再尝试从getter方法获取
        if (property.getMember() != null) {
            annotation = property.getMember().getAnnotation(annotationType);
        }
        
        return annotation;
    }
}
