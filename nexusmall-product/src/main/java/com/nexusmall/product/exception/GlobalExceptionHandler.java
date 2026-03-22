package com.nexusmall.product.exception;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleProductNotFound(ProductNotFoundException ex) {
        return Result.failure(CommonResultCode.NOT_FOUND.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        return Result.failure(CommonResultCode.SYSTEM_ERROR);
    }
}
