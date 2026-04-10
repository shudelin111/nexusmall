package com.nexusmall.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.constant.ApiVersionConstants;
import com.nexusmall.common.enums.ApiVersionEnum;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API 版本拦截器（生产级增强版）
 * <p>
 * 业界标准改进：
 * 1. 使用枚举管理版本号，避免魔法字符串
 * 2. 支持版本兼容性检查（高版本可访问低版本接口）
 * 3. 添加注解缓存，提升性能
 * 4. 提供废弃版本警告
 * 5. 返回规范的错误码和提示信息
 * </p>
 * <p>
 * 版本兼容策略：
 * - 请求 v2，接口标注 v1 → ✅ 允许访问（向后兼容）
 * - 请求 v1，接口标注 v2 → ❌ 拒绝访问（向前不兼容）
 * - 请求 v3（不支持）→ ❌ 拒绝访问
 * - 未指定版本 → 使用最新稳定版本
 * </p>
 *
 * @author shudl
 * @since 2026-04-09
 */
@Slf4j
public class ApiVersionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 注解缓存：key=方法签名, value=ApiVersion注解
     * 避免每次请求都进行反射操作
     */
    private static final ConcurrentHashMap<String, ApiVersion> ANNOTATION_CACHE = new ConcurrentHashMap<>(256);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 1. 获取并验证请求版本
        String requestedVersion = resolveRequestedVersion(request, response);
        if (requestedVersion == null) {
            return false; // 版本验证失败，已写入响应
        }

        // 2. 获取目标接口的版本要求
        String targetVersion = resolveTargetVersion(handlerMethod);

        // 3. 如果接口未标注版本，默认支持所有版本
        if (targetVersion == null) {
            log.debug("【API版本】接口未标注版本，默认支持所有版本 | path={}", request.getRequestURI());
            setResponseHeaders(response, requestedVersion);
            return true;
        }

        // 4. 版本兼容性检查
        if (!isVersionCompatible(requestedVersion, targetVersion)) {
            log.warn("【API版本】版本不兼容 | 请求版本={}, 接口最低版本={}, path={}",
                    requestedVersion, targetVersion, request.getRequestURI());

            writeErrorResponse(response, ResultCode.API_VERSION_NOT_SUPPORTED,
                    String.format("当前接口需要 %s 或更高版本，您使用的是 %s", targetVersion, requestedVersion));
            return false;
        }

        // 5. 检查是否为废弃版本
        checkDeprecatedVersion(requestedVersion);

        log.debug("【API版本】版本验证通过 | version={}, path={}", requestedVersion, request.getRequestURI());

        // 6. 设置响应头
        setResponseHeaders(response, requestedVersion);

        return true;
    }

    /**
     * 解析请求版本
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return 有效的版本号，无效返回null
     */
    private String resolveRequestedVersion(HttpServletRequest request, HttpServletResponse response) {
        String version = request.getHeader(ApiVersionConstants.HEADER_API_VERSION);

        // 未指定版本，使用最新稳定版本
        if (version == null || version.trim().isEmpty()) {
            version = ApiVersionEnum.getLatest().getVersion();
            log.debug("【API版本】未指定版本，使用最新版本: {}", version);
        }

        // 验证版本格式
        if (!ApiVersionEnum.isValid(version)) {
            log.warn("【API版本】不支持的版本: {}, 支持的版本: v1, v2", version);
            writeErrorResponse(response, ResultCode.API_VERSION_NOT_SUPPORTED,
                    String.format("不支持的API版本: %s，支持的版本: v1, v2", version));
            return null;
        }

        return version;
    }

    /**
     * 解析目标接口的版本要求（带缓存）
     *
     * @param handlerMethod 处理器方法
     * @return 目标版本号，未标注返回null
     */
    private String resolveTargetVersion(HandlerMethod handlerMethod) {
        // 生成缓存key
        String cacheKey = generateCacheKey(handlerMethod);

        // 从缓存获取
        ApiVersion annotation = ANNOTATION_CACHE.computeIfAbsent(cacheKey, key -> {
            // 方法级别注解优先
            ApiVersion methodAnnotation = AnnotationUtils.findAnnotation(
                    handlerMethod.getMethod(), ApiVersion.class
            );
            if (methodAnnotation != null) {
                return methodAnnotation;
            }

            // 其次类级别注解
            return AnnotationUtils.findAnnotation(
                    handlerMethod.getBeanType(), ApiVersion.class
            );
        });

        return annotation != null ? annotation.value() : null;
    }

    /**
     * 检查版本兼容性
     * <p>
     * 兼容规则：
     * - 请求版本 >= 接口版本 → ✅ 允许
     * - 请求版本 < 接口版本 → ❌ 拒绝
     * </p>
     *
     * @param requestedVersion 请求版本
     * @param targetVersion    接口要求的最低版本
     * @return true=兼容
     */
    private boolean isVersionCompatible(String requestedVersion, String targetVersion) {
        ApiVersionEnum requested = ApiVersionEnum.fromString(requestedVersion);
        ApiVersionEnum target = ApiVersionEnum.fromString(targetVersion);

        if (requested == null || target == null) {
            return false;
        }

        // 请求版本必须 >= 接口要求的最低版本
        return requested.isGreaterOrEqual(target);
    }

    /**
     * 检查废弃版本并记录警告日志
     *
     * @param version 版本号
     */
    private void checkDeprecatedVersion(String version) {
        ApiVersionEnum versionEnum = ApiVersionEnum.fromString(version);
        if (versionEnum != null && versionEnum.isDeprecated()) {
            log.warn("【API版本】使用了已废弃的版本: {}, 建议升级到: {}",
                    version, ApiVersionEnum.getLatest().getVersion());
        }
    }

    /**
     * 生成注解缓存key
     *
     * @param handlerMethod 处理器方法
     * @return 缓存key
     */
    private String generateCacheKey(HandlerMethod handlerMethod) {
        return handlerMethod.getBeanType().getName() + "#" + handlerMethod.getMethod().getName();
    }

    /**
     * 设置响应头
     *
     * @param response HTTP响应
     * @param version  API版本
     */
    private void setResponseHeaders(HttpServletResponse response, String version) {
        response.setHeader("X-API-Version", version);
        response.setHeader("X-API-Supported-Versions", "v1, v2");
        response.setHeader("X-API-Latest-Version", ApiVersionEnum.getLatest().getVersion());
    }

    /**
     * 写入错误响应
     *
     * @param response HTTP响应
     * @param resultCode 错误码
     * @param message    错误消息
     */
    @SuppressWarnings("deprecation")
    private void writeErrorResponse(HttpServletResponse response, ResultCode resultCode, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            response.setContentType("application/json;charset=UTF-8");

            // 使用自定义消息创建失败响应（兼容旧API）
            Result<Void> result = Result.failure(resultCode.getErrorCode(), message);
            OBJECT_MAPPER.writeValue(response.getWriter(), result);
        } catch (Exception e) {
            log.error("【API版本】写入错误响应失败", e);
        }
    }
}
