package com.nexusmall.auth.application.service;

/**
 * Token 黑名单服务接口 (基于 Redis)
 * <p>
 * 业界标准：
 * - JWT 本身不可撤销,通过黑名单实现即时失效
 * - Access Token 登出时加入黑名单
 * - Refresh Token 撤销时加入黑名单
 * - 黑名单过期时间 = Token 剩余有效期
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
public interface TokenBlacklistService {

    /**
     * 将 Token 加入黑名单
     *
     * @param jti         Token JTI
     * @param expireTime  过期时间(毫秒)
     */
    void addToBlacklist(String jti, long expireTime);

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param jti Token JTI
     * @return true=在黑名单中
     */
    boolean isBlacklisted(String jti);

    /**
     * 从黑名单中移除 Token (可选,通常不需要)
     *
     * @param jti Token JTI
     */
    void removeFromBlacklist(String jti);
}
