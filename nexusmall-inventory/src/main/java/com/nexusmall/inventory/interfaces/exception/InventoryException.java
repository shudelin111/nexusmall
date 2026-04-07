package com.nexusmall.inventory.interfaces.exception;

import com.nexusmall.common.exception.NexusmallException;

/**
 * 库存业务异常
 *
 * @author shudl
 * @since 2026-04-06
 */
public class InventoryException extends NexusmallException {

    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(Integer code, String message) {
        super(code, message);
    }
}
