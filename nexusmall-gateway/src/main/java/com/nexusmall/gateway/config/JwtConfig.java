package com.nexusmall.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置类
 * <p>
 * 用于配置 JWT Token 的密钥和过期时间
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.jwt")
public class JwtConfig {

    /**
     * JWT 签名密钥
     */
    private String secretKey;

    /**
     * Token 过期时间（毫秒）
     */
    private Long expireTimeInMs;
}
