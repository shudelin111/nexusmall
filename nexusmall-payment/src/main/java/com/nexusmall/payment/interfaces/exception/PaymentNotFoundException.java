package com.nexusmall.payment.interfaces.exception;

/**
 * 支付单未找到异常
 *
 * @author shudl
 * @since 2026-04-06
 */
public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String paymentNo) {
        super("支付单不存在：" + paymentNo);
    }
    
    public PaymentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
