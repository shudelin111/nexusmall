package com.nexusmall.auth.exception;

public class AuthException extends RuntimeException {
    
    private Integer code;
    
    public AuthException(String message) {
        super(message);
    }
    
    public AuthException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AuthException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
}
