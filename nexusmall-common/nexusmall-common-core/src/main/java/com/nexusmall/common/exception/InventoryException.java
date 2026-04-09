package com.nexusmall.common.exception;

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

    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(Integer code, String message) {
        super(code, message);
    }

    public InventoryException(String code, String message) {
        super(code, message);
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public InventoryException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
