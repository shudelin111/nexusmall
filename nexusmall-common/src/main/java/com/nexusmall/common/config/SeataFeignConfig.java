package com.nexusmall.common.config;

import feign.RequestInterceptor;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Seata Feign 拦截器配置（全局）
 * 用于在 Feign 调用时自动传递 Seata XID
 */
@Configuration
public class SeataFeignConfig {

    private static final Logger log = LoggerFactory.getLogger(SeataFeignConfig.class);

    @Bean
    public RequestInterceptor seataRequestInterceptor() {
        return requestTemplate -> {
            // 方式 1：从 Seata RootContext 获取当前 XID（推荐）
            String xid = RootContext.getXID();
            if (xid != null && !xid.isEmpty()) {
                requestTemplate.header("XID", xid);
                log.info("[Seata-Feign] 传递 XID: {}", xid);
            } else {
                // 方式 2：从 HTTP Header 获取（兼容网关透传场景）
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    xid = request.getHeader("XID");
                    if (xid != null && !xid.isEmpty()) {
                        requestTemplate.header("XID", xid);
                        log.info("[Seata-Feign] 从 HTTP Header 传递 XID: {}", xid);
                    }
                }
            }
        };
    }
}
