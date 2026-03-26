package com.nexusmall.auth.vo;

import lombok.Data;

/**
 * 认证请求 VO
 * <p>
 * 用于接收用户登录请求的用户名和密码
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Data
public class AuthRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
