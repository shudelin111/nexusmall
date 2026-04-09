package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 购物车业务异常
 * <p>
 * 用于购物车模块的业务异常，如购物车项不存在、库存不足等
 * </p>
 *
 * @author nexusmall
 */
public class CartException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public CartException(ResultCode resultCode) {
        super(resultCode);
    }

    public CartException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
