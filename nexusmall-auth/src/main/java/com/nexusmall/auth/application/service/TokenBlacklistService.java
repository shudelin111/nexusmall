package com.nexusmall.auth.application.service;

/**
 * Token 黑名单服务接?(基于 Redis)
 * <p>
 * 业界标准：
 * - JWT 本身不可撤销,通过黑名单实现即时失?
 * - Access Token 登出时加入黑名单
 * - Refresh Token 撤销时加入黑名单
 * - 黑名单过期时?= Token 剩余有效?
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
public interface TokenBlacklistService {

    /**
     * ?Token 加入黑名?
     *
     * @param jti         Token JTI
     * @param expireTime  过期时间(毫秒)
     */
    void addToBlacklist(String jti, long expireTime);

    /**
     * 检?Token 是否在黑名单?
     *
     * @param jti Token JTI
     * @return true=在黑名单?
     */
    boolean isBlacklisted(String jti);

    /**
     * 从黑名单中移?Token (可?通常不需?
     *
     * @param jti Token JTI
     */
    void removeFromBlacklist(String jti);
}
