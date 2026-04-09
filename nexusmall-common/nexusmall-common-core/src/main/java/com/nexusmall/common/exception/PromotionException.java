package com.nexusmall.common.exception;

/**
 * 营销业务异常
 * <p>
 * 用于营销模块的业务异常，如优惠券不存在、活动已结束等
 * </p>
 *
 * @author nexusmall
 */
public class PromotionException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public PromotionException(String message) {
        super(message);
    }

    public PromotionException(Integer code, String message) {
        super(code, message);
    }

    public PromotionException(String code, String message) {
        super(code, message);
    }

    public PromotionException(String message, Throwable cause) {
        super(message, cause);
    }

    public PromotionException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public PromotionException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
