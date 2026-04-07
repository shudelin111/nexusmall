package com.nexusmall.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Gateway JWT RSA 公钥配置
 * <p>
 * 业界标准：
 * - 网关作为 Resource Server，只需要公钥验证 Token
 * - 私钥仅存在于 auth 服务
 * - 公钥从 Nacos 配置中心读取
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.jwt.rsa")
public class GatewayJwtConfig {

    /**
     * RSA公钥 (PEM 格式)
     * 用于验证 JWT Token 签名
     */
    private String publicKey;

    /**
     * JWT 签发者标识
     */
    private String issuer = "nexusmall-auth";

    /**
     * JWT 受众标识
     */
    private String audience = "nexusmall-services";
}
