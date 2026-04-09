package com.nexusmall.common.exception;

/**
 * 支付业务异常
 * <p>
 * 用于支付模块的业务异常，如支付单不存在、支付失败等
 * </p>
 *
 * @author nexusmall
 */
public class PaymentException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(Integer code, String message) {
        super(code, message);
    }

    public PaymentException(String code, String message) {
        super(code, message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public PaymentException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
