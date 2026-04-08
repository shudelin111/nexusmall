package com.nexusmall.common.exception;

/**
 * 订单业务异常
 * <p>
 * 用于订单模块的业务异常，如订单不存在、库存不足等
 * </p>
 *
 * @author nexusmall
 */
public class OrderException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public OrderException(String message) {
        super(message);
    }

    public OrderException(Integer code, String message) {
        super(code, message);
    }

    public OrderException(String code, String message) {
        super(code, message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public OrderException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
