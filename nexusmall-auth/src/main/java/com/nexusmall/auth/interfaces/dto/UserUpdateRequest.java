package com.nexusmall.auth.interfaces.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

/**
 * 用户更新请求
 *
 * @author nexusmall
 * @since 2026-04-08
 */
@Data
public class UserUpdateRequest {

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 状态（0=禁用，1=启用）
     */
    @Pattern(regexp = "^[01]$", message = "状态值不正确")
    private String status;
}
