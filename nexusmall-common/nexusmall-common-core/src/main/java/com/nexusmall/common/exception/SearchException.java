package com.nexusmall.common.exception;

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

    public SearchException(String message) {
        super(message);
    }

    public SearchException(Integer code, String message) {
        super(code, message);
    }

    public SearchException(String code, String message) {
        super(code, message);
    }

    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }

    public SearchException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public SearchException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
