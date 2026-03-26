package com.nexusmall.auth.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    
    private final String code;
    
    public AuthException(String message) {
        super(message);
        this.code = null;
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
        this.code = null;
    }
    
    public AuthException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = String.valueOf(code);
    }
    
    public AuthException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
