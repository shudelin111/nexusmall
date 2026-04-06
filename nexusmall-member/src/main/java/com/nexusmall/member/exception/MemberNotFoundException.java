package com.nexusmall.member.exception;

/**
 * 会员未找到异常
 *
 * @author shudl
 * @since 2026-04-06
 */
public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long memberId) {
        super("会员不存在，ID: " + memberId);
    }
    
    public MemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
