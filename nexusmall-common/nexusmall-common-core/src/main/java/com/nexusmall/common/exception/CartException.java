package com.nexusmall.common.exception;

/**
 * 购物车业务异常
 * <p>
 * 用于购物车模块的业务异常，如购物车项不存在、库存不足等
 * </p>
 *
 * @author nexusmall
 */
public class CartException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public CartException(String message) {
        super(message);
    }

    public CartException(Integer code, String message) {
        super(code, message);
    }

    public CartException(String code, String message) {
        super(code, message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }

    public CartException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public CartException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
