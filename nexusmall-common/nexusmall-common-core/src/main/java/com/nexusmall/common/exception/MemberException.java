package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 会员业务异常
 * <p>
 * 用于会员模块的业务异常，如会员不存在、会员已禁用等
 * </p>
 *
 * @author nexusmall
 */
public class MemberException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public MemberException(ResultCode resultCode) {
        super(resultCode);
    }

    public MemberException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
