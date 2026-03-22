package com.nexusmall.order.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderSn) {
        super("订单不存在: " + orderSn);
    }
}
