package com.nexusmall.payment.exception;

/**
 * 支付业务异常
 *
 * @author shudl
 * @since 2026-04-06
 */
public class PaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    public PaymentException(String message) {
        super(message);
        this.code = 500;
    }

    public PaymentException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
