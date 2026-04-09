package com.nexusmall.product.config;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Seata XID 拦截?
 * 用于?HTTP Header 中提?XID 并绑定到 RootContext
 */
@Component
public class SeataHandlerInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SeataHandlerInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ?HTTP Header 获取 XID
        String xid = request.getHeader("XID");
        if (xid != null && !xid.isEmpty()) {
            RootContext.bind(xid);
            log.info("====== Product 服务?HTTP Header 中绑?XID: {} ======", xid);
        } else {
            log.info("====== Product 服务未从 HTTP Header 中找?XID ======");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清?XID
        String xid = RootContext.getXID();
        if (xid != null) {
            RootContext.unbind();
            log.debug("====== Product 服务已解?XID: {} ======", xid);
        }
    }
}
