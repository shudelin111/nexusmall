package com.nexusmall.auth.application.service;

import com.nexusmall.auth.domain.entity.RefreshToken;
import com.nexusmall.auth.domain.entity.User;

/**
 * Refresh Token 服务接口
 * <p>
 * 业界标准?
 * - 支持刷新 Access Token
 * - 支持主动撤销 (用户登出)
 * - 支持设备管理 (查看/撤销特定设备?Token)
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
public interface RefreshTokenService {

    /**
     * 保存 Refresh Token
     *
     * @param user       用户信息
     * @param token      Refresh Token (JWT)
     * @param jti        Token JTI
     * @param deviceInfo 设备信息
     * @param ipAddress  IP 地址
     */
    void saveRefreshToken(User user, String token, String jti, String deviceInfo, String ipAddress);

    /**
     * 验证并刷?Access Token
     * <p>
     * 流程?
     * 1. 验证 Refresh Token 有效?
     * 2. 检查是否在黑名单中
     * 3. 生成新的 Access Token + Refresh Token
     * 4. 使旧 Refresh Token 失效
     * 5. 保存新的 Refresh Token
     * </p>
     *
     * @param refreshToken Refresh Token (JWT)
     * @return 新的 Access Token
     */
    String refreshAccessToken(String refreshToken);

    /**
     * 撤销指定?Refresh Token (单设备登?
     *
     * @param userId 用户ID
     * @param jti    Token JTI
     */
    void revokeRefreshToken(Long userId, String jti);

    /**
     * 撤销用户的所?Refresh Token (所有设备登?
     *
     * @param userId 用户ID
     */
    void revokeAllRefreshTokens(Long userId);

    /**
     * 检?Refresh Token 是否在黑名单?
     *
     * @param jti Token JTI
     * @return true=在黑名单?已撤销)
     */
    boolean isTokenRevoked(String jti);

    /**
     * 清理过期?Refresh Token (定时任务调用)
     *
     * @return 清理数量
     */
    int cleanExpiredTokens();
}
