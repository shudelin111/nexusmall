package com.nexusmall.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Seata XID 透传过滤器（Gateway 层）
 * <p>
 * 业界标准：Gateway 不需要参与分布式事务，只需透传 XID 到下游服务
 * 官方文档: https://seata.io/zh-cn/docs/user/quickstart.html
 * </p>
 *
 * <h3>执行顺序说明：</h3>
 * <ul>
 *   <li>Spring Security (-100) - 先执行身份认证</li>
 *   <li>SeataXidFilter (-90) - 后透传 XID，确保认证通过后再处理事务</li>
 *   <li>路由转发 (0+) - 最后转发请求</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <p>Gateway 不引入 Seata 依赖，因此使用硬编码常量值 {@code "TX_XID"}
 * （对应 Seata 的 {@code RootContext.KEY_XID}）。</p>
 *
 * @author shudl
 * @since 2026-04-04
 */
@Slf4j
@Component
@Order(-90)  // 在 Spring Security (-100) 之后，路由转发 (0+) 之前执行
public class SeataXidFilter implements GlobalFilter {

    /**
     * Seata XID 请求头名称
     * <p>对应 Seata 的 RootContext.KEY_XID = "TX_XID"</p>
     * <p>Gateway 不引入 Seata 依赖，因此使用硬编码常量值</p>
     */
    private static final String SEATA_XID_HEADER = "TX_XID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String xid = request.getHeaders().getFirst(SEATA_XID_HEADER);

        // 构建新请求，透传 XID 到下游服务
        ServerHttpRequest newRequest = request.mutate()
                .header(SEATA_XID_HEADER, xid != null ? xid : "")
                .build();

        if (xid != null && !xid.isEmpty()) {
            log.debug("[Seata-Gateway] 透传 XID: {}", xid);
        }

        return chain.filter(exchange.mutate().request(newRequest).build());
    }
}
