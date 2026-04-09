package com.nexusmall.common.exception;

/**
 * 通知业务异常
 * <p>
 * 用于通知模块的业务异常，如通知发送失败、模板不存在等
 * </p>
 *
 * @author nexusmall
 */
public class NotificationException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(Integer code, String message) {
        super(code, message);
    }

    public NotificationException(String code, String message) {
        super(code, message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public NotificationException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
