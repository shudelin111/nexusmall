package com.nexusmall.common.feign;

import com.nexusmall.common.constant.ApiVersionConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign API 版本拦截器
 * <p>
 * 业界标准：
 * - 微服务间调用时，自动透传 API 版本号
 * - 从当前请求的 Header 中提取 X-API-Version
 * - 添加到 Feign 请求的 Header 中
 * - 避免在每个 Feign 方法中重复声明 @RequestHeader
 * </p>
 *
 * @author shudl
 * @since 2026-04-08
 */
public class FeignApiVersionInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignApiVersionInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                
                // 从当前请求中提取 API 版本号
                String apiVersion = request.getHeader(ApiVersionConstants.HEADER_API_VERSION);
                
                if (apiVersion != null && !apiVersion.isEmpty()) {
                    // 透传到下游服务
                    template.header(ApiVersionConstants.HEADER_API_VERSION, apiVersion);
                    log.debug("[Feign-API-Version] 透传 API 版本: {} -> {}", apiVersion, template.url());
                } else {
                    // 如果没有显式指定，使用默认版本
                    template.header(ApiVersionConstants.HEADER_API_VERSION, ApiVersionConstants.CURRENT_VERSION);
                    log.debug("[Feign-API-Version] 使用默认 API 版本: {} -> {}", ApiVersionConstants.CURRENT_VERSION, template.url());
                }
            } else {
                // 非 HTTP 请求上下文（如定时任务、消息消费者），使用默认版本
                template.header(ApiVersionConstants.HEADER_API_VERSION, ApiVersionConstants.CURRENT_VERSION);
                log.debug("[Feign-API-Version] 无 HTTP 上下文，使用默认 API 版本: {} -> {}", ApiVersionConstants.CURRENT_VERSION, template.url());
            }
        } catch (Exception e) {
            log.warn("[Feign-API-Version] 提取 API 版本失败，使用默认版本", e);
            template.header(ApiVersionConstants.HEADER_API_VERSION, ApiVersionConstants.CURRENT_VERSION);
        }
    }
}
