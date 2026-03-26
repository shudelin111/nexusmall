package com.nexusmall.common.exception;

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

    public ThirdPartyException(String message) {
        super(message);
    }

    public ThirdPartyException(Integer code, String message) {
        super(code, message);
    }

    public ThirdPartyException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThirdPartyException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
