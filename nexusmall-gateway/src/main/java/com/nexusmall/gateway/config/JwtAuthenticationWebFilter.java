package com.nexusmall.gateway.config;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.GatewayException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtConfig jwtConfig;

    public JwtAuthenticationWebFilter(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 跳过公开路径（登录、注册等）
        String path = exchange.getRequest().getPath().value();
        if (path.startsWith("/auth/login") || 
            path.startsWith("/auth/register") || 
            path.startsWith("/auth/validate")) {
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
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

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
                    token,
                    AuthorityUtils.createAuthorityList(authorities.toArray(new String[0]))
            );
            log.info("JWT 认证成功，username: {}, roles: {}, permissions: {}", 
                    username, roles != null ? roles.size() : 0, permissions != null ? permissions.size() : 0);
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
        } catch (Exception ex) {
            log.error("Gateway Token 验证失败，path: {}, token: {}, 错误：{}", 
                     exchange.getRequest().getPath(), token, ex.getMessage(), ex);
            return Mono.error(new GatewayException(CommonResultCode.UNAUTHORIZED.getCode(), "Token 验证失败：" + ex.getMessage(), ex));
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
