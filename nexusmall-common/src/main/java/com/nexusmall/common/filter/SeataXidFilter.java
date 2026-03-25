package com.nexusmall.common.filter;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Seata XID 过滤器（全局）
 * 用于从 HTTP Header 中提取 XID 并绑定到 RootContext
 * 
 * 使用 OncePerRequestFilter 确保每个请求只执行一次
 * 自动被 Spring Boot 扫描并注册，无需手动配置 WebConfig
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // 高优先级，确保在 Spring Security 等 Filter 之前执行
public class SeataXidFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SeataXidFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 从 HTTP Header 获取 XID
        String xid = request.getHeader("XID");
        
        if (xid != null && !xid.isEmpty()) {
            // 绑定 XID 到 RootContext
            RootContext.bind(xid);
            log.info("[Seata-Filter] 从 HTTP Header 中绑定 XID: {}", xid);
            
            try {
                // 继续执行后续过滤器和请求处理
                filterChain.doFilter(request, response);
            } finally {
                // 请求完成后清理 XID
                RootContext.unbind();
                log.debug("[Seata-Filter] 已解绑 XID: {}", xid);
            }
        } else {
            // 没有 XID，直接继续执行
            log.debug("[Seata-Filter] 未从 HTTP Header 中找到 XID");
            filterChain.doFilter(request, response);
        }
    }
}
