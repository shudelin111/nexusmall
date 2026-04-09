package com.nexusmall.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token 实体?
 * <p>
 * 业界标准：
 * - Refresh Token 需要持久化存储 (数据?Redis)
 * - 支持主动撤销 (用户登出时删?
 * - 记录设备信息 (用于安全管理)
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_refresh_token")
public class RefreshToken {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户?
     */
    private String username;

    /**
     * Refresh Token (JWT)
     */
    private String token;

    /**
     * Token JTI (唯一标识，用于黑名单)
     */
    private String jti;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 设备信息 (可?
     */
    private String deviceInfo;

    /**
     * IP 地址 (可?
     */
    private String ipAddress;

    /**
     * 是否有效 (0=无效, 1=有效)
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
