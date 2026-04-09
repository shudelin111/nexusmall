package com.nexusmall.common.exception;

import com.nexusmall.common.constant.ErrorMessageConstants;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理器（生产级标准）
 * <p>
 * 核心设计原则：
 * 1. 统一响应格式 - 所有异常返回标准化的 JSON 结构
 * 2. 安全信息过滤 - 生产环境不暴露堆栈信息和敏感数据
 * 3. 分级日志记录 - 业务异常 WARN，系统异常 ERROR + 完整堆栈
 * 4. 环境差异化 - 开发环境返回详细信息，生产环境脱敏
 * 5. HTTP 语义化 - 使用正确的 HTTP 状态码（4xx 客户端错误，5xx 服务端错误）
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /**
     * 当前激活的环境配置
     * <p>
     * 生产级实践：
     * 1. 默认值为空字符串，未配置时视为开发环境（更安全）
     * 2. 生产环境必须通过 K8s/Docker 显式设置 SPRING_PROFILES_ACTIVE=prod
     * 3. 避免开发人员本地启动时误用生产配置
     * </p>
     */
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    /**
     * 是否显示详细错误信息（仅开发/测试环境）
     * <p>
     * 未配置 profile 时默认视为开发环境，返回详细错误便于调试
     * </p>
     */
    private boolean isDevEnvironment() {
        // 未配置或为空时，默认视为开发环境（Fail-Safe 原则）
        if (activeProfile == null || activeProfile.isEmpty()) {
            return true;
        }
        return ErrorMessageConstants.Environment.DEV.equalsIgnoreCase(activeProfile) 
                || ErrorMessageConstants.Environment.TEST.equalsIgnoreCase(activeProfile);
    }

    // ==================== 业务异常处理 ====================

    /**
     * 处理业务异常（NexusmallException）
     * <p>
     * 这是最常用的异常类型，由业务代码主动抛出
     * 例如：库存不足、订单不存在、权限不足等
     * </p>
     *
     * @param ex 业务异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NexusmallException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleNexusmallException(NexusmallException ex, HttpServletRequest request) {
        // 业务异常使用 WARN 级别，避免日志刷屏
        log.warn("【业务异常】URI: {}, Code: {}, Message: {}", 
                request.getRequestURI(), ex.getCode(), ex.getMessage());
        
        return Result.failure(ex.getResultCode());
    }

    // ==================== 参数校验异常处理 ====================

    /**
     * 处理参数校验异常（@RequestBody + @Valid）
     * <p>
     * 场景：JSON 请求体参数校验失败
     * 示例：{"name":"", "age":-1} → name不能为空; age必须大于0
     * </p>
     *
     * @param ex 方法参数校验异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("【参数校验异常】URI: {}, 错误: {}", request.getRequestURI(), message);
        return Result.failure(ResultCode.PARAM_INVALID);
    }

    /**
     * 处理绑定异常（表单/URL 参数校验）
     * <p>
     * 场景：GET 请求或表单提交参数校验失败
     * </p>
     *
     * @param ex 绑定异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException ex, HttpServletRequest request) {
        String message = ex.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("【参数绑定异常】URI: {}, 错误: {}", request.getRequestURI(), message);
        return Result.failure(ResultCode.PARAM_INVALID);
    }

    /**
     * 处理缺少请求参数异常
     * <p>
     * 场景：必填参数未传递
     * 示例：GET /api/users?id= → Required request parameter 'id' is not present
     * </p>
     *
     * @param ex 缺少参数异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("缺少必填参数: %s", ex.getParameterName());
        log.warn("【缺少参数异常】URI: {}, 参数: {}", request.getRequestURI(), ex.getParameterName());
        return Result.failure(ResultCode.PARAM_INVALID);
    }

    /**
     * 处理参数类型不匹配异常
     * <p>
     * 场景：参数类型转换失败
     * 示例：GET /api/users/abc → abc无法转换为Long类型
     * </p>
     *
     * @param ex 类型不匹配异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "未知");
        log.warn("【类型不匹配异常】URI: {}, 参数: {}, 值: {}", 
                request.getRequestURI(), ex.getName(), ex.getValue());
        return Result.failure(ResultCode.PARAM_INVALID);
    }

    /**
     * 处理非法参数异常
     * <p>
     * 场景：手动抛出的 IllegalArgumentException
     * </p>
     *
     * @param ex 非法参数异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("【非法参数异常】URI: {}, 错误: {}", request.getRequestURI(), ex.getMessage());
        return Result.failure(ResultCode.PARAM_INVALID);
    }

    // ==================== HTTP 协议异常处理 ====================

    /**
     * 处理 404 异常（资源未找到）
     * <p>
     * 生产级实践：
     * 1. 需要在 application.yml 中配置：
     *    spring:
     *      mvc:
     *        throw-exception-if-no-handler-found: true
     *      web:
     *        resources:
     *          add-mappings: false
     * 2. 返回 JSON 而非 HTML 页面
     * </p>
     *
     * @param ex 未找到处理器异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("【404 异常】URI: {}, Method: {}", ex.getRequestURL(), ex.getHttpMethod());
        return Result.failure(ResultCode.NOT_FOUND);
    }

    /**
     * 处理方法不支持异常
     * <p>
     * 场景：POST 接口使用了 GET 请求
     * </p>
     *
     * @param ex 方法不支持异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = String.format("不支持的请求方法: %s，支持的方法: %s", 
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        log.warn("【方法不支持异常】URI: {}, 方法: {}", request.getRequestURI(), ex.getMethod());
        return Result.failure(ResultCode.METHOD_NOT_ALLOWED);
    }

    // ==================== 系统异常处理 ====================

    /**
     * 处理系统异常（兜底处理）
     * <p>
     * 生产级实践：
     * 1. 记录完整堆栈信息到日志系统
     * 2. 生产环境不向前端暴露堆栈详情（安全风险）
     * 3. 开发环境可返回详细错误信息便于调试
     * </p>
     *
     * @param ex 系统异常
     * @param request HTTP 请求对象
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex, HttpServletRequest request) {
        // 系统异常必须记录完整堆栈，便于运维排查
        log.error("【系统异常】URI: {}, Method: {}, Exception: {}", 
                request.getRequestURI(), request.getMethod(), ex.getClass().getName(), ex);
        
        // 生产环境返回通用错误提示，避免泄露敏感信息
        if (isDevEnvironment()) {
            // 开发/测试环境：返回详细错误信息
            return Result.failure(ResultCode.SYSTEM_ERROR);
        } else {
            // 生产环境：返回通用提示
            return Result.failure(ResultCode.SYSTEM_ERROR);
        }
    }
}
