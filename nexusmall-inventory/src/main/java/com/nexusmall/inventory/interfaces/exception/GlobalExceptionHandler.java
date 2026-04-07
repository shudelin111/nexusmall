package com.nexusmall.inventory.interfaces.exception;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * 全局异常处理器
 * <p>
 * 统一处理库存服务的所有异常，返回标准化的错误响应
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理库存业务异常
     *
     * @param ex 库存异常
     * @return 错误响应
     */
    @ExceptionHandler(InventoryException.class)
    @ResponseStatus(BAD_REQUEST)
    public Result<Void> handleInventoryException(InventoryException ex, HttpServletRequest request) {
        log.error("库存业务异常: URI={}, Message={}", request.getRequestURI(), ex.getMessage(), ex);
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常（@Validated）
     *
     * @param ex 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() == null
                ? "参数校验失败"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数校验失败：{}", message, ex);
        return Result.failure(CommonResultCode.PARAM_INVALID);
    }

    /**
     * 处理绑定异常
     *
     * @param ex 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(BAD_REQUEST)
    public Result<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldError() == null
                ? "参数绑定失败"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数绑定失败：{}", message, ex);
        return Result.failure(CommonResultCode.PARAM_INVALID);
    }

    /**
     * 处理非法参数异常
     *
     * @param ex 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.error("参数异常: URI={}, Message={}", request.getRequestURI(), ex.getMessage(), ex);
        return Result.failure(CommonResultCode.PARAM_INVALID);
    }

    /**
     * 处理系统异常
     *
     * @param ex 系统异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex, HttpServletRequest request) {
        log.error("系统异常: URI={}", request.getRequestURI(), ex);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
