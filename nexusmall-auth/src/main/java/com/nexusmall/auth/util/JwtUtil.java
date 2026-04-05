package com.nexusmall.auth.util;

import com.nexusmall.auth.config.RsaKeyProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT 工具类 (RS256 非对称加密)
 * <p>
 * 业界标准：
 * - 使用 RS256 算法 (RSA + SHA256)
 * - 私钥签发 Token，公钥验证 Token
 * - 强制验证算法白名单，防止算法混淆攻击
 * - 包含标准 Claims (iss, sub, aud, exp, iat, jti)
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 允许的签名算法白名单 (防止算法混淆攻击)
     */
    private static final String ALLOWED_ALGORITHM = "RS256";

    @Autowired
    private RsaKeyProperties rsaKeyProperties;

    /**
     * 生成 Access Token
     *
     * @param username    用户名
     * @param roles       角色列表
     * @param permissions 权限列表
     * @return JWT Token
     */
    public String generateAccessToken(String username, List<String> roles, List<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + rsaKeyProperties.getAccessTokenExpireTime());
        String jti = UUID.randomUUID().toString(); // JWT ID，用于防重放和黑名单

        return Jwts.builder()
                // 标准 Claims
                .setIssuer(rsaKeyProperties.getIssuer())           // iss: 签发者
                .setAudience(rsaKeyProperties.getAudience())       // aud: 受众
                .setSubject(username)                               // sub: 主题(用户名)
                .setId(jti)                                         // jti: 唯一标识
                .setIssuedAt(now)                                   // iat: 签发时间
                .setExpiration(expiryDate)                          // exp: 过期时间
                // 自定义 Claims
                .claim("roles", roles)
                .claim("permissions", permissions)
                .claim("token_type", "access")                      // 标记为 Access Token
                // 签名 (RS256)
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 生成 Refresh Token
     *
     * @param username 用户名
     * @return Refresh Token
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + rsaKeyProperties.getRefreshTokenExpireTime());
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setIssuer(rsaKeyProperties.getIssuer())
                .setAudience(rsaKeyProperties.getAudience())
                .setSubject(username)
                .setId(jti)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("token_type", "refresh")  // 标记为 Refresh Token
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从 Token 中获取 JTI (用于黑名单)
     *
     * @param token JWT Token
     * @return JTI
     */
    public String getJtiFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    /**
     * 从 Token 中获取角色列表
     *
     * @param token JWT Token
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roles", List.class);
    }

    /**
     * 从 Token 中获取权限列表
     *
     * @param token JWT Token
     * @return 权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("permissions", List.class);
    }

    /**
     * 验证 Token 是否有效
     *
     * @param token JWT Token
     * @return true=有效, false=无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception ex) {
            log.error("JWT 验证失败，token: {}, 错误：{}", token, ex.getMessage(), ex);
            return false;
        }
    }

    /**
     * 检查 Token 是否为 Access Token
     *
     * @param token JWT Token
     * @return true=Access Token
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "access".equals(claims.get("token_type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查 Token 是否为 Refresh Token
     *
     * @param token JWT Token
     * @return true=Refresh Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("token_type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 解析 Token (强制验证算法白名单)
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                // 关键：强制指定允许的算法，防止算法混淆攻击
                .requireIssuer(rsaKeyProperties.getIssuer())      // 验证签发者
                .requireAudience(rsaKeyProperties.getAudience())  // 验证受众
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取 RSA 私钥 (用于签名)
     *
     * @return PrivateKey
     */
    private PrivateKey getPrivateKey() {
        try {
            String privateKeyPEM = rsaKeyProperties.getPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            log.error("加载 RSA 私钥失败", e);
            throw new RuntimeException("加载 RSA 私钥失败", e);
        }
    }

    /**
     * 获取 RSA 公钥 (用于验证)
     *
     * @return PublicKey
     */
    private PublicKey getPublicKey() {
        try {
            String publicKeyPEM = rsaKeyProperties.getPublicKey()
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("加载 RSA 公钥失败", e);
            throw new RuntimeException("加载 RSA 公钥失败", e);
        }
    }
}
