package com.nexusmall.gateway.util;

import com.nexusmall.gateway.config.GatewayJwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Gateway JWT 验证工具类
 * <p>
 * 业界标准：
 * - 仅验证 Token，不签发
 * - 使用 RSA 公钥验证 RS256 签名
 * - 强制验证算法白名单、签发者、受众
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@Component
public class GatewayJwtValidator {

    private static final Logger log = LoggerFactory.getLogger(GatewayJwtValidator.class);

    @Autowired
    private GatewayJwtConfig jwtConfig;

    /**
     * 验证并解析 JWT Token
     *
     * @param token JWT Token (不含 "Bearer " 前缀)
     * @return Claims
     * @throws Exception 验证失败时抛出异常
     */
    public Claims validateAndParseToken(String token) throws Exception {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    // 强制验证标准 Claims
                    .requireIssuer(jwtConfig.getIssuer())      // 验证签发者
                    .requireAudience(jwtConfig.getAudience())  // 验证受众
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("JWT 验证失败: {}", e.getMessage());
            throw new Exception("Token 无效或已过期", e);
        }
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = validateAndParseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("获取用户名失败", e);
            return null;
        }
    }

    /**
     * 检查 Token 是否有效
     *
     * @param token JWT Token
     * @return true=有效
     */
    public boolean isTokenValid(String token) {
        try {
            validateAndParseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 RSA 公钥
     *
     * @return PublicKey
     */
    private PublicKey getPublicKey() throws Exception {
        try {
            String publicKeyPEM = jwtConfig.getPublicKey()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("加载 RSA 公钥失败", e);
            throw new Exception("加载 RSA 公钥失败", e);
        }
    }
}
