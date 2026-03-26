package com.nexusmall.common.exception;

/**
 * 商品业务异常
 * <p>
 * 用于商品模块的业务异常，如商品不存在、库存不足等
 * </p>
 *
 * @author nexusmall
 */
public class ProductException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public ProductException(String message) {
        super(message);
    }

    public ProductException(Integer code, String message) {
        super(code, message);
    }

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProductException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
