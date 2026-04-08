package com.nexusmall.common.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign 请求拦截器
 * <p>
 * 业界标准：
 * - 微服务间调用时，自动透传当前用户的认证信息
 * - 从当前请求的 Header 中提取用户信息
 * - 添加到 Feign 请求的 Header 中
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignAuthInterceptor.class);

    /**
     * Header 名称常量
     */
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    @Override
    public void apply(RequestTemplate template) {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes == null) {
                log.debug("不在 HTTP 请求上下文中，跳过透传用户信息");
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            // 1. 透传 Authorization Token (如果需要)
            String authHeader = request.getHeader(HEADER_AUTHORIZATION);
            if (authHeader != null && !authHeader.isEmpty()) {
                template.header(HEADER_AUTHORIZATION, authHeader);
            }

            // 2. 透传用户信息
            String username = request.getHeader(HEADER_USER_NAME);
            if (username != null && !username.isEmpty()) {
                template.header(HEADER_USER_NAME, username);
            }

            String roles = request.getHeader(HEADER_USER_ROLES);
            if (roles != null && !roles.isEmpty()) {
                template.header(HEADER_USER_ROLES, roles);
            }

            String permissions = request.getHeader(HEADER_USER_PERMISSIONS);
            if (permissions != null && !permissions.isEmpty()) {
                template.header(HEADER_USER_PERMISSIONS, permissions);
            }

            log.debug("Feign 请求已透传用户信息，username: {}", username);

        } catch (Exception e) {
            log.error("Feign 请求拦截器执行失败", e);
        }
    }
}
