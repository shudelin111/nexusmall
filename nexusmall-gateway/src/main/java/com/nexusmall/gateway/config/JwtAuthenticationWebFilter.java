package com.nexusmall.gateway.config;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.GatewayException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器
 * <p>
 * 用于验证请求中的 JWT Token，解析用户信息并设置安全上下文
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
// @Component  // 已废弃：使用 JwtAuthGlobalFilter (RSA-256) 替代
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtConfig jwtConfig;

    public JwtAuthenticationWebFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * 执行过滤器逻辑
     * <p>
     * 检查请求路径、解析 Authorization 头、验证 JWT Token
     * </p>
     *
     * @param exchange ServerWebExchange
     * @param chain WebFilterChain
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 跳过公开路径（登录、注册等）
        String path = exchange.getRequest().getPath().value();
        if (isPublicPath(path)) {
            log.debug("跳过公开路径：{}", path);
            return chain.filter(exchange);
        }
        
        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            log.debug("请求头中缺少 Authorization 或格式不正确，path: {}", path);
            return chain.filter(exchange);
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            Claims claims = parseAndValidateToken(token);
            return processValidToken(claims, exchange, chain);
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token 已过期，path: {}, 过期时间：{}", path, e.getClaims().getExpiration());
            return Mono.error(createGatewayException(CommonResultCode.UNAUTHORIZED, "Token 已过期"));
        } catch (SignatureException e) {
            log.warn("JWT Token 签名无效，path: {}, 原因：{}", path, e.getMessage());
            return Mono.error(createGatewayException(CommonResultCode.UNAUTHORIZED, "Token 签名无效"));
        } catch (JwtException e) {
            log.warn("JWT Token 解析失败，path: {}, 原因：{}", path, e.getMessage());
            return Mono.error(createGatewayException(CommonResultCode.UNAUTHORIZED, "Token 解析失败"));
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token 格式非法，path: {}, 原因：{}", path, e.getMessage());
            return Mono.error(createGatewayException(CommonResultCode.UNAUTHORIZED, "Token 格式非法"));
        } catch (Exception e) {
            log.error("JWT Token 验证异常，path: {}, token: {}", path, token, e);
            return Mono.error(createGatewayException(CommonResultCode.UNAUTHORIZED, "Token 验证失败"));
        }
    }

    /**
     * 判断是否为公开路径（无需认证）
     *
     * @param path 请求路径
     * @return true-是公开路径，false-需要认证
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/auth/login") || 
               path.startsWith("/auth/register") || 
               path.startsWith("/auth/validate");
    }

    /**
     * 解析并验证 JWT Token
     *
     * @param token JWT 令牌
     * @return Claims 对象
     * @throws JwtException JWT 验证异常
     */
    private Claims parseAndValidateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 处理有效的 JWT Token
     *
     * @param claims JWT 声明信息
     * @param exchange ServerWebExchange
     * @param chain WebFilterChain
     * @return Mono<Void>
     */
    private Mono<Void> processValidToken(Claims claims, ServerWebExchange exchange, WebFilterChain chain) {
        String username = claims.getSubject();
        
        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);
        
        List<String> authorities = new ArrayList<>();
        if (roles != null) {
            authorities.addAll(roles);
        }
        if (permissions != null) {
            authorities.addAll(permissions);
        }
        
        if (authorities.isEmpty()) {
            authorities.add("ROLE_USER");
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                claims.getId(),
                AuthorityUtils.createAuthorityList(authorities.toArray(new String[0]))
        );
        log.info("JWT 认证成功，username: {}, roles: {}, permissions: {}", 
                username, roles != null ? roles.size() : 0, permissions != null ? permissions.size() : 0);
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    /**
     * 创建 GatewayException
     *
     * @param code 错误码
     * @param message 错误消息
     * @return GatewayException
     */
    private GatewayException createGatewayException(CommonResultCode code, String message) {
        return new GatewayException(code.getErrorCode(), message);
    }

    /**
     * 获取签名密钥
     *
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
