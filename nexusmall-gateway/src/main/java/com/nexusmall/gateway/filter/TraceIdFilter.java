package com.nexusmall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 链路追踪ID过滤器（生产级标准）
 * <p>
 * 功能：
 * 1. 从请求头提取 traceId（如果存在）
 * 2. 如果不存在，生成新的 traceId
 * 3. 将 traceId 添加到请求头，透传给下游服务
 * 4. 将 traceId 添加到响应头，返回给客户端
 * </p>
 * <p>
 * 遵循业界最佳实践：
 * - 在网关层统一处理 traceId
 * - 通过 HTTP Header 传递，而非嵌入响应体
 * - 支持分布式全链路追踪
 * </p>
 *
 * @author nexusmall
 * @since 2026-04-09
 */
@Slf4j
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    /**
     * 请求/响应头中的 traceId 键名
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 1. 尝试从请求头获取 traceId（由上游网关或客户端传递）
        String traceId = request.getHeaders().getFirst(TRACE_ID_HEADER);

        // 2. 如果不存在，生成新的 traceId
        if (!StringUtils.hasText(traceId)) {
            traceId = generateTraceId();
            log.debug("【网关】生成新的 traceId: {}", traceId);
        } else {
            log.debug("【网关】使用传入的 traceId: {}", traceId);
        }

        // 3. 构建新的请求，添加 traceId 到请求头（透传给下游服务）
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(TRACE_ID_HEADER, traceId)
                .build();

        // 4. 构建新的响应，添加 traceId 到响应头（返回给客户端）
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(TRACE_ID_HEADER, traceId);

        log.debug("【网关】traceId 已注入，path: {}, traceId: {}", 
                request.getPath(), traceId);

        // 5. 继续过滤链
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // 最高优先级，确保在其他过滤器之前执行
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 生成 traceId
     * <p>
     * 格式：UUID（去除横线）+ 时间戳后6位
     * 示例：a1b2c3d4e5f6789012345678901234ab123456
     * </p>
     *
     * @return traceId
     */
    private String generateTraceId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        long timestamp = System.currentTimeMillis() % 1000000;
        return uuid + String.format("%06d", timestamp);
    }
}
