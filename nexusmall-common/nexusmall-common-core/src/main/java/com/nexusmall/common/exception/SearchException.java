package com.nexusmall.common.exception;

import com.nexusmall.common.enums.ResultCode;

/**
 * 搜索业务异常
 * <p>
 * 用于搜索模块的业务异常，如搜索失败、索引异常等
 * </p>
 *
 * @author nexusmall
 */
public class SearchException extends NexusmallException {

    private static final long serialVersionUID = 1L;

    public SearchException(ResultCode resultCode) {
        super(resultCode);
    }

    public SearchException(ResultCode resultCode, Throwable cause) {
        super(resultCode, cause);
    }
}
