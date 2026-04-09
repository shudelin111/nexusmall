package com.nexusmall.auth.infrastructure.persistence.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nexusmall.auth.domain.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Refresh Token Mapper
 *
 * @author shudl
 * @since 2026-04-05
 */
@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {

    /**
     * 根据 JTI 查询 Refresh Token
     *
     * @param jti Token JTI
     * @return RefreshToken
     */
    RefreshToken findByJti(@Param("jti") String jti);

    /**
     * 根据用户名查询有效的 Refresh Token 列表
     *
     * @param username 用户�?
     * @return RefreshToken 列表
     */
    List<RefreshToken> findValidByUserId(@Param("userId") Long userId);

    /**
     * 使指定用户的某个 Token 失效
     *
     * @param userId 用户ID
     * @param jti    Token JTI
     * @return 影响行数
     */
    int invalidateToken(@Param("userId") Long userId, @Param("jti") String jti);

    /**
     * 使指定用户的所�?Token 失效 (登出所有设�?
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int invalidateAllTokens(@Param("userId") Long userId);

    /**
     * 清理过期�?Refresh Token
     *
     * @return 影响行数
     */
    int cleanExpiredTokens();
}
