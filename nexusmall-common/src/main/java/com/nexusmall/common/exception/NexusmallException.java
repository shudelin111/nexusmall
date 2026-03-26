package com.nexusmall.common.exception;

/**
 * Nexusmall 业务异常基类
 * <p>
 * 所有业务相关的异常都应该继承此类，便于统一处理和识别
 * </p>
 *
 * @author nexusmall
 */
public class NexusmallException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    public NexusmallException(String message) {
        super(message);
    }

    public NexusmallException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public NexusmallException(String message, Throwable cause) {
        super(message, cause);
    }

    public NexusmallException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
