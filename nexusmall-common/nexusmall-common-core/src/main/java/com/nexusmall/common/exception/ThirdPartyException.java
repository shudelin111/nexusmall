package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 第三方服务业务异常
 * <p>
 * 用于第三方服务模块的业务异常，如短信发送失败、OSS 上传失败等
 * </p>
 *
 * @author nexusmall
 */
public class ThirdPartyException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public ThirdPartyException(ResultCode resultCode) {
        super(resultCode);
    }

    public ThirdPartyException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
