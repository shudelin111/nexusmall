package com.nexusmall.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * XSS防护过滤器
 * <p>
 * 生产级实践：拦截所有HTTP请求，对参数进行XSS清理
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
@Order(1)  // 最高优先级，在其他过滤器之前执行
public class XssFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("【XSS过滤器】初始化完成");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        String uri = request.getRequestURI();
        
        // 跳过静态资源和API文档
        if (isStaticResource(uri)) {
            chain.doFilter(req, res);
            return;
        }
        
        log.debug("【XSS过滤器】处理请求: {}", uri);
        
        // 使用XSS包装器包装请求
        XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(request);
        chain.doFilter(xssRequest, res);
    }

    @Override
    public void destroy() {
        log.info("【XSS过滤器】销毁");
    }

    /**
     * 判断是否为静态资源（跳过XSS检查）
     */
    private boolean isStaticResource(String uri) {
        return uri.startsWith("/static/")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".gif")
                || uri.endsWith(".ico")
                || uri.contains("/swagger")
                || uri.contains("/api-docs")
                || uri.contains("/doc.html");
    }
}
