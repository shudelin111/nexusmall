package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

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

    public PromotionException(ResultCode resultCode) {
        super(resultCode);
    }

    public PromotionException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
