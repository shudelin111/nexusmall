package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

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

    public NotificationException(ResultCode resultCode) {
        super(resultCode);
    }

    public NotificationException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
