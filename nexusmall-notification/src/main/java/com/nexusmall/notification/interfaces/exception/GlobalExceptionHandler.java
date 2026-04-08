package com.nexusmall.notification.exception;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Notification 模块全局异常处理器
 * <p>
 * 统一处理通知服务的所有异常
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数异常
     *
     * @param ex IllegalArgumentException
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("参数异常：{}", ex.getMessage(), ex);
        return Result.failure(CommonResultCode.PARAM_INVALID);
    }

    /**
     * 处理参数校验异常
     *
     * @param ex MethodArgumentNotValidException
     * @return 统一响应结果
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldError() == null
                ? "参数校验失败"
                : ex.getBindingResult().getFieldError().getDefaultMessage();
        log.error("参数校验失败：{}", message, ex);
        return Result.failure(CommonResultCode.PARAM_INVALID);
    }

    /**
     * 处理系统异常
     *
     * @param ex Exception
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常：", ex);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
