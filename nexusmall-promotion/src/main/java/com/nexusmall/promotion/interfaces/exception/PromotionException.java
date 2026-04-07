package com.nexusmall.promotion.interfaces.exception;

import com.nexusmall.common.exception.NexusmallException;

/**
 * 营销业务异常
 * <p>
 * 业界标准：继承统一异常基类，支持错误码和消息
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
public class PromotionException extends NexusmallException {

    public PromotionException(String message) {
        super(message);
    }

    public PromotionException(Integer code, String message) {
        super(code, message);
    }
}
