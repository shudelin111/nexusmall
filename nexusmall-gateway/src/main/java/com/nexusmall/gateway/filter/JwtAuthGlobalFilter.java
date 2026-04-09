package com.nexusmall.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.gateway.util.GatewayJwtValidator;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局 JWT 认证过滤器
 * <p>
 * 业界标准：
 * - 在网关层统一验证 JWT，下游微服务信任网关
 * - 验证失败返回 401 Unauthorized
 * - 将用户信息通过 Header 透传给下游服务
 * - 白名单路径跳过验证 (登录、注册等公开接口)
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGlobalFilter.class);

    /**
     * 白名单路径 (不需要认证)
     */
    private static final List<String> WHITE_LIST = java.util.Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/actuator",
            "/doc.html",
            "/swagger",
            "/v3/api-docs"
    );

    @Autowired
    private GatewayJwtValidator jwtValidator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        log.info("JWT 过滤器 - 请求路径: {}, 方法: {}", path, request.getMethod());

        // 1. 检查是否在白名单中
        if (isWhiteListPath(path)) {
            log.info("白名单路径，跳过认证: {}", path);
            return chain.filter(exchange);
        }
        
        log.info("非白名单路径，开始验证 JWT: {}", path);

        // 2. 获取 Authorization Header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("缺少或无效的 Authorization Header，path: {}", path);
            return unauthorizedResponse(exchange, "未提供有效的认证令牌");
        }

        String token = authHeader.substring(7);

        // 3. 验证 Token
        try {
            Claims claims = jwtValidator.validateAndParseToken(token);
            
            // 4. 提取用户信息
            String username = claims.getSubject();
            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            @SuppressWarnings("unchecked")
            List<String> permissions = claims.get("permissions", List.class);

            // 5. 将用户信息添加到 Request Header，透传给下游服务
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Name", username)
                    .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                    .header("X-User-Permissions", permissions != null ? String.join(",", permissions) : "")
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            log.debug("JWT 验证成功，用户: {}, path: {}", username, path);
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("JWT 验证失败，path: {}, 错误: {}", path, e.getMessage());
            return unauthorizedResponse(exchange, "认证令牌无效或已过期");
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteListPath(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 返回 401 Unauthorized 响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<Void> result = Result.failure(ResultCode.UNAUTHORIZED);

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(result);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        // 优先级最高，在其他过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
