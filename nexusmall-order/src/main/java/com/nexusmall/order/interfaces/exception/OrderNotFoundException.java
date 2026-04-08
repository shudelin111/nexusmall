package com.nexusmall.order.interfaces.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderSn) {
        super("订单不存在：" + orderSn);
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
