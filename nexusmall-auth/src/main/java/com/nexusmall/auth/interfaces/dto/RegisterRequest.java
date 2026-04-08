package com.nexusmall.auth.interfaces.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户注册请求
 * <p>
 * 生产级特性：
 * - JSR-303 参数校验
 * - 密码强度验证
 * - 邮箱格式验证
 * </p>
 *
 * @author nexusmall
 * @since 2026-04-08
 */
@Data
public class RegisterRequest {

    /**
     * 用户名（4-20位字母数字下划线）
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "用户名格式不正确（4-20位字母数字下划线）")
    private String username;

    /**
     * 密码（8-20位，包含字母和数字）
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$", 
             message = "密码格式不正确（8-20位，必须包含字母和数字）")
    private String password;

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
     * 角色 ID 列表（可选）
     */
    private List<Long> roleIds;
}
