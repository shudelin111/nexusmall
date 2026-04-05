package com.nexusmall.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Kafka 日志收集配置属性
 * 
 * <p>业界标准实践：使用 @ConfigurationProperties 进行类型安全的配置绑定</p>
 * 
 * <p>配置示例：</p>
 * <pre>
 * nexusmall:
 *   logging:
 *     kafka:
 *       enabled: false
 * </pre>
 * 
 * <p>环境变量覆盖：</p>
 * <ul>
 *   <li>Windows: {@code set NEXUSMALL_LOGGING_KAFKA_ENABLED=true}</li>
 *   <li>Linux/Mac: {@code export NEXUSMALL_LOGGING_KAFKA_ENABLED=true}</li>
 *   <li>Docker/K8s: {@code NEXUSMALL_LOGGING_KAFKA_ENABLED=true}</li>
 * </ul>
 * 
 * @author shudl
 * @since 2026-04-05
 */
@Component
@ConfigurationProperties(prefix = "nexusmall.logging.kafka")
public class KafkaLoggingProperties {
    
    /**
     * 是否启用 Kafka 日志收集
     * 默认值：false（禁用）
     */
    private boolean enabled = false;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
