package com.nexusmall.cart.interfaces.exception;

import com.nexusmall.common.enums.ResultCode;
import lombok.Getter;

/**
 * 购物车业务异常
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
public class CartException extends RuntimeException {

    private final ResultCode resultCode;

    public CartException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public CartException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public CartException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }
}
