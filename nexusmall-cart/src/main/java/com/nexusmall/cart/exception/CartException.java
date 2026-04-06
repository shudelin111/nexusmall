package com.nexusmall.cart.exception;

import com.nexusmall.common.enums.CommonResultCode;
import lombok.Getter;

/**
 * 购物车业务异常
 *
 * @author shudl
 * @since 2026-04-06
 */
@Getter
public class CartException extends RuntimeException {

    private final CommonResultCode resultCode;

    public CartException(CommonResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public CartException(CommonResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }

    public CartException(CommonResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.resultCode = resultCode;
    }
}
