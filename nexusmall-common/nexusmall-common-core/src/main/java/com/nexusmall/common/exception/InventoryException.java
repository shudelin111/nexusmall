package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 库存业务异常
 * <p>
 * 用于库存模块的业务异常，如库存不足、库存扣减失败等
 * </p>
 *
 * @author nexusmall
 */
public class InventoryException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public InventoryException(ResultCode resultCode) {
        super(resultCode);
    }

    public InventoryException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
