package com.nexusmall.auth.application.service.impl;

import com.nexusmall.auth.application.service.TokenBlacklistService;
import com.nexusmall.common.util.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现(基于 Redis)(基于 Redis)
 * <p>
 * 业界标准：
 * - 使用 Redis SET 结构存储黑名单
 * - Key 格式: token:blacklist:{jti}
 * - TTL = Token 剩余有效期(自动过期清理)
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistServiceImpl.class);

    /**
     * Redis Key 前缀
     */
    private static final String BLACKLIST_KEY_PREFIX = "token:blacklist:";

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void addToBlacklist(String jti, long expireTime) {
        if (jti == null || jti.isEmpty()) {
            log.warn("JTI 为空，无法加入黑名单");
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + jti;
        
        // ?JTI 存入 Redis，设置过期时间
        // 过期时间 = Token 剩余有效期，确保黑名单不会永久存在
        redisUtils.set(key, "1", expireTime, TimeUnit.MILLISECONDS);
        
        log.debug("Token 已加入黑名单，jti: {}, 过期时间: {}ms", jti, expireTime);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isEmpty()) {
            return false;
        }

        String key = BLACKLIST_KEY_PREFIX + jti;
        Boolean exists = redisUtils.hasKey(key);
        
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void removeFromBlacklist(String jti) {
        if (jti == null || jti.isEmpty()) {
            return;
        }

        String key = BLACKLIST_KEY_PREFIX + jti;
        redisUtils.delete(key);
        
        log.debug("Token 已从黑名单移除，jti: {}", jti);
    }
}
