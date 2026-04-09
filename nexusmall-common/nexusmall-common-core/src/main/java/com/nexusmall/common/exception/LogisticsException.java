package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

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

    public LogisticsException(ResultCode resultCode) {
        super(resultCode);
    }

    public LogisticsException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
