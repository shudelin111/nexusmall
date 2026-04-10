package com.nexusmall.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RSA 密钥配置
 * <p>
 * 业界标准：使用 RS256 非对称加密算法
 * - 私钥用于签发 Token（仅 auth 服务持有）
 * - 公钥用于验证 Token（分发给所有微服务和网关）
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@Data
@Component
@ConfigurationProperties(prefix = "security.jwt.rsa")
public class RsaKeyProperties {

    /**
     * RSA 私钥 (PEM 格式)
     * 用于签发 JWT Token
     * 必须严格保密，仅 auth 服务可访问
     */
    private String privateKey;

    /**
     * RSA 公钥 (PEM 格式)
     * 用于验证 JWT Token
     * 可公开分发给所有微服务和网关
     */
    private String publicKey;

    /**
     * Access Token 过期时间 (毫秒)
     * 业界标准: 15-30 分钟
     * 默认: 30 分钟 (1800000ms)
     */
    private long accessTokenExpireTime = 1800000L;

    /**
     * Refresh Token 过期时间 (毫秒)
     * 业界标准: 7 天
     * 默认: 7 天(604800000ms)
     */
    private long refreshTokenExpireTime = 604800000L;

    /**
     * JWT 签发者标识
     * 用于验证 Token 来源
     */
    private String issuer = "nexusmall-auth";

    /**
     * JWT 受众标识
     * 用于限制 Token 的使用范围
     */
    private String audience = "nexusmall-services";
}
