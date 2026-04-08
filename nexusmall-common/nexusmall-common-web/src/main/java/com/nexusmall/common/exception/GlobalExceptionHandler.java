package com.nexusmall.common.exception;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 统一处理所有业务异常，包括 Sentinel 流控异常
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // 最高优先级
public class GlobalExceptionHandler {

    /**
     * 处理 Sentinel 流控异常
     *
     * @param ex SentinelFlowException
     * @return 统一响应结果
     */
    @ExceptionHandler(SentinelFlowException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public Result<Void> handleSentinelFlowException(SentinelFlowException ex) {
        log.warn("【Sentinel 流控异常】资源：{}, 消息：{}", ex.getResourceName(), ex.getMessage());
        return Result.failure(CommonResultCode.SYSTEM_BUSY);
    }

    /**
     * 处理业务异常
     *
     * @param ex NexusmallException
     * @return 统一响应结果
     */
    @ExceptionHandler(NexusmallException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleNexusmallException(NexusmallException ex) {
        log.error("【业务异常】code: {}, message: {}", ex.getCode(), ex.getMessage());
        String code = ex.getCode() != null ? ex.getCode() : CommonResultCode.SYSTEM_ERROR.getErrorCode();
        return Result.failure(code, ex.getMessage());
    }

    /**
     * 处理参数校验异常
     *
     * @param ex IllegalArgumentException
     * @return 统一响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("【参数异常】{}", ex.getMessage());
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
        log.error("【系统异常】", ex);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
