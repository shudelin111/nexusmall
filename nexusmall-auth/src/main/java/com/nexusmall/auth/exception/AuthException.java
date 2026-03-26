package com.nexusmall.auth.exception;

public class AuthException extends RuntimeException {
    
    private String code;
    
    public AuthException(String message) {
        super(message);
    }
    
    public AuthException(Integer code, String message) {
        super(message);
        this.code = String.valueOf(code);
    }
    
    public AuthException(String code, String message) {
        super(message);
        this.code = code;
    }
    
    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AuthException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = String.valueOf(code);
    }
    
    public AuthException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
}
