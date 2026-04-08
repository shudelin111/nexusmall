package com.nexusmall.auth.application.service.impl;

import com.nexusmall.auth.infrastructure.persistence.dao.RefreshTokenMapper;
import com.nexusmall.auth.infrastructure.persistence.dao.UserMapper;
import com.nexusmall.auth.domain.entity.RefreshToken;
import com.nexusmall.auth.domain.entity.User;
import com.nexusmall.auth.interfaces.exception.AuthException;
import com.nexusmall.auth.application.service.RefreshTokenService;
import com.nexusmall.auth.application.service.TokenBlacklistService;
import com.nexusmall.auth.util.JwtUtil;
import com.nexusmall.common.constant.ErrorMessageConstants;
import com.nexusmall.common.enums.CommonResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Refresh Token 服务实现
 *
 * @author shudl
 * @since 2026-04-05
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenServiceImpl.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenMapper refreshTokenMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenBlacklistService blacklistService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRefreshToken(User user, String token, String jti, String deviceInfo, String ipAddress) {
        // 计算过期时间
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(
                604800L // 7 天
        );

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .token(token)
                .jti(jti)
                .expireTime(expireTime)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .status(1) // 有效
                .build();

        refreshTokenMapper.insert(refreshToken);
        log.info("Refresh Token 已保存，userId: {}, username: {}, jti: {}", user.getId(), user.getUsername(), jti);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String refreshAccessToken(String refreshTokenStr) {
        // 1. 验证 Refresh Token 格式和签名
        if (!jwtUtil.validateToken(refreshTokenStr)) {
            throw new AuthException(CommonResultCode.TOKEN_INVALID.getCode(), "Refresh Token 无效");
        }

        // 2. 检查是否为 Refresh Token
        if (!jwtUtil.isRefreshToken(refreshTokenStr)) {
            throw new AuthException(CommonResultCode.TOKEN_INVALID.getCode(), "不是有效的 Refresh Token");
        }

        // 3. 获取 JTI 并检查是否在黑名单中
        String jti = jwtUtil.getJtiFromToken(refreshTokenStr);
        if (blacklistService.isBlacklisted(jti)) {
            throw new AuthException(CommonResultCode.TOKEN_EXPIRED.getCode(), "Refresh Token 已被撤销");
        }

        // 4. 从数据库查询 Refresh Token
        RefreshToken storedToken = refreshTokenMapper.findByJti(jti);
        if (storedToken == null || storedToken.getStatus() != 1) {
            throw new AuthException(CommonResultCode.TOKEN_EXPIRED.getCode(), "Refresh Token 不存在或已失效");
        }

        // 5. 检查是否过期
        if (storedToken.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new AuthException(CommonResultCode.TOKEN_EXPIRED.getCode(), "Refresh Token 已过期");
        }

        // 6. 获取用户信息
        User user = userMapper.selectById(storedToken.getUserId());
        if (user == null || user.getStatus() != 1) {
            throw new AuthException(CommonResultCode.USER_NOT_FOUND.getCode(), "用户不存在或已被禁用");
        }

        // 7. 生成新的 Access Token + Refresh Token
        String newAccessToken = jwtUtil.generateAccessToken(
                user.getUsername(),
                Collections.emptyList(), // TODO: 从数据库加载角色和权限
                Collections.emptyList()
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String newJti = jwtUtil.getJtiFromToken(newRefreshToken);

        // 8. 使旧 Refresh Token 失效
        revokeRefreshToken(storedToken.getUserId(), jti);

        // 9. 保存新的 Refresh Token
        saveRefreshToken(user, newRefreshToken, newJti, storedToken.getDeviceInfo(), storedToken.getIpAddress());

        log.info("Access Token 已刷新，userId: {}, username: {}", user.getId(), user.getUsername());
        return newAccessToken;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeRefreshToken(Long userId, String jti) {
        // 1. 数据库标记为无效
        int rows = refreshTokenMapper.invalidateToken(userId, jti);
        
        // 2. 加入 Redis 黑名单
        // 获取 Token 剩余有效期
        RefreshToken token = refreshTokenMapper.findByJti(jti);
        if (token != null) {
            long remainingTime = java.time.Duration.between(
                    LocalDateTime.now(), 
                    token.getExpireTime()
            ).toMillis();
            
            if (remainingTime > 0) {
                blacklistService.addToBlacklist(jti, remainingTime);
            }
        }

        log.info("Refresh Token 已撤销，userId: {}, jti: {}, 影响行数: {}", userId, jti, rows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeAllRefreshTokens(Long userId) {
        // 1. 获取所有有效的 Token JTI
        java.util.List<RefreshToken> validTokens = refreshTokenMapper.findValidByUserId(userId);
        
        // 2. 将所有 JTI 加入黑名单
        for (RefreshToken token : validTokens) {
            long remainingTime = java.time.Duration.between(
                    LocalDateTime.now(), 
                    token.getExpireTime()
            ).toMillis();
            
            if (remainingTime > 0) {
                blacklistService.addToBlacklist(token.getJti(), remainingTime);
            }
        }

        // 3. 数据库批量标记为无效
        int rows = refreshTokenMapper.invalidateAllTokens(userId);
        
        log.info("用户所有 Refresh Token 已撤销，userId: {}, 影响行数: {}", userId, rows);
    }

    @Override
    public boolean isTokenRevoked(String jti) {
        return blacklistService.isBlacklisted(jti);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredTokens() {
        int count = refreshTokenMapper.cleanExpiredTokens();
        log.info("已清理过期 Refresh Token，数量: {}", count);
        return count;
    }
}
