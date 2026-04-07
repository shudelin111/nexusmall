package com.nexusmall.member.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 会员资料更新请求
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "会员资料更新请求")
public class MemberProfileUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatar;

    @Schema(description = "性别：0=未知，1=男，2=女")
    private Integer gender;

    @Schema(description = "生日")
    private String birthday;
}
