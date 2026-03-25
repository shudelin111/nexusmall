package com.nexusmall.product.config;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Seata XID 拦截器
 * 用于从 HTTP Header 中提取 XID 并绑定到 RootContext
 */
@Component
public class SeataHandlerInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SeataHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 HTTP Header 获取 XID
        String xid = request.getHeader("XID");
        if (xid != null && !xid.isEmpty()) {
            RootContext.bind(xid);
            log.info("====== Product 服务从 HTTP Header 中绑定 XID: {} ======", xid);
        } else {
            log.info("====== Product 服务未从 HTTP Header 中找到 XID ======");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理 XID
        String xid = RootContext.getXID();
        if (xid != null) {
            RootContext.unbind();
            log.debug("====== Product 服务已解绑 XID: {} ======", xid);
        }
    }
}
