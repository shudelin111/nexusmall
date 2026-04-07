package com.nexusmall.cart.interfaces.exception;

import com.nexusmall.common.enums.CommonResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局异常处理器
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理购物车业务异常
     */
    @ExceptionHandler(CartException.class)
    public Result<?> handleCartException(CartException e) {
        log.error("【购物车业务异常】{}", e.getMessage(), e);
        return Result.failure(e.getResultCode());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("【系统异常】", e);
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
