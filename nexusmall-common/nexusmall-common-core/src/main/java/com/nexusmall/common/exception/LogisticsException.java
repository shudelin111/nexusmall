package com.nexusmall.common.exception;

/**
 * 物流业务异常
 * <p>
 * 用于物流模块的业务异常，如物流单不存在、物流状态异常等
 * </p>
 *
 * @author nexusmall
 */
public class LogisticsException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public LogisticsException(String message) {
        super(message);
    }

    public LogisticsException(Integer code, String message) {
        super(code, message);
    }

    public LogisticsException(String code, String message) {
        super(code, message);
    }

    public LogisticsException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogisticsException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public LogisticsException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
