package com.nexusmall.member.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员信息实体类
 * <p>
 * 业界标准：
 * - Auth 模块：sys_user 通过 user_id 关联
 * - 存储用户业务数据（昵称/头像/会员等级等）
 * - 不存储认证信息（密码由Auth 管理）
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ums_member")
public class Member {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID（关联 sys_user.id）
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 性别 0=未知 1=男，2=女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 会员等级 ID
     */
    private Long memberLevelId;

    /**
     * 成长值
     */
    private Integer growthPoint;

    /**
     * 积分
     */
    private Integer integration;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 状态：0=禁用 1=正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
