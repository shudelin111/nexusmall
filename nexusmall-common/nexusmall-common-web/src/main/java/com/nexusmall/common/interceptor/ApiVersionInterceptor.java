package com.nexusmall.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.constant.ApiVersionConstants;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * API 版本拦截器
 * <p>
 * 功能：
 * 1. 从请求 Header 中提取 API 版本号
 * 2. 验证版本号是否支持
 * 3. 根据版本号路由到对应的 Controller/Method
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 1. 获取请求中的 API 版本
        String requestedVersion = request.getHeader(ApiVersionConstants.HEADER_API_VERSION);

        // 2. 如果未指定版本，使用默认版本
        if (requestedVersion == null || requestedVersion.trim().isEmpty()) {
            requestedVersion = ApiVersionConstants.CURRENT_VERSION;
            log.debug("【API 版本】未指定版本，使用默认版本：{}", requestedVersion);
        }

        // 3. 验证版本是否支持
        if (!isSupportedVersion(requestedVersion)) {
            log.warn("【API 版本】不支持的版本：{}, 支持的版本：{}", 
                    requestedVersion, Arrays.toString(ApiVersionConstants.SUPPORTED_VERSIONS));
            
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.setContentType("application/json;charset=UTF-8");
            Result<Void> result = Result.failure(
                    ResultCode.SYSTEM_ERROR
            );
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        // 4. 检查 Controller/Method 是否有 @ApiVersion 注解
        ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(
                handlerMethod.getMethod(), ApiVersion.class
        );
        
        ApiVersion classAnnotation = AnnotationUtils.findAnnotation(
                handlerMethod.getBeanType(), ApiVersion.class
        );

        // 5. 方法级别注解优先于类级别注解
        String targetVersion = null;
        if (methodAnnotation != null) {
            targetVersion = methodAnnotation.value();
        } else if (classAnnotation != null) {
            targetVersion = classAnnotation.value();
        }

        // 6. 如果没有标注版本，默认支持所有版本
        if (targetVersion == null) {
            log.debug("【API 版本】接口未标注版本，默认支持所有版本，path: {}", request.getRequestURI());
            return true;
        }

        // 7. 验证请求版本与目标版本是否匹配
        if (!requestedVersion.equals(targetVersion)) {
            log.warn("【API 版本】版本不匹配，请求版本：{}, 目标版本：{}, path: {}", 
                    requestedVersion, targetVersion, request.getRequestURI());
            
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            Result<Void> result = Result.failure(
                    ResultCode.NOT_FOUND
            );
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        log.debug("【API 版本】版本验证通过，version: {}, path: {}", requestedVersion, request.getRequestURI());
        
        // 在响应头中添加 API 版本信息(方便客户端识别)
        response.setHeader("X-API-Version", requestedVersion);
        response.setHeader("X-API-Supported-Versions", String.join(",", ApiVersionConstants.SUPPORTED_VERSIONS));
        
        return true;
    }

    /**
     * 检查版本是否支持
     *
     * @param version 版本号
     * @return true=支持，false=不支持
     */
    private boolean isSupportedVersion(String version) {
        return Arrays.asList(ApiVersionConstants.SUPPORTED_VERSIONS).contains(version);
    }
}
