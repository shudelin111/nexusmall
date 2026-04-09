package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 商品业务异常
 * <p>
 * 用于商品模块的业务异常，如商品不存在、库存不足等
 * </p>
 *
 * @author nexusmall
 */
public class ProductException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public ProductException(ResultCode resultCode) {
        super(resultCode);
    }

    public ProductException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
