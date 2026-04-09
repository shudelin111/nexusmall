package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 网关业务异常
 * <p>
 * 用于网关模块的业务异常，如 Token 验证失败、路由错误等
 * </p>
 *
 * @author nexusmall
 */
public class GatewayException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public GatewayException(ResultCode resultCode) {
        super(resultCode);
    }

    public GatewayException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
