package com.nexusmall.auth.vo;

import lombok.Data;

import java.util.List;

/**
 * 认证响应 VO
 * <p>
 * 返回用户认证成功后的 Token、过期时间、角色和权限信息
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
@Data
public class AuthResponse {

    /**
     * Access Token (JWT)
     * 用于 API 请求认证
     */
    private String token;

    /**
     * Refresh Token (JWT)
     * 用于刷新 Access Token
     */
    private String refreshToken;

    /**
     * 过期时间（毫秒）
     */
    private Long expireTime;

    /**
     * 用户名
     */
    private String username;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 权限列表
     */
    private List<String> permissions;
}
