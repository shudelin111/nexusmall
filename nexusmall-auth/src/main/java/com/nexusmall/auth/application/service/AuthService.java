package com.nexusmall.auth.application.service;

import com.nexusmall.auth.domain.entity.Role;
import com.nexusmall.auth.domain.entity.User;
import com.nexusmall.auth.interfaces.dto.AuthRequest;
import com.nexusmall.auth.interfaces.dto.AuthResponse;

import java.util.List;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    void logout(String token);
    boolean validateToken(String token);
    
    /**
     * 刷新 Access Token
     *
     * @param refreshToken Refresh Token
     * @return 新的 Access Token
     */
    String refreshAccessToken(String refreshToken);
    
    /**
     * 用户注册
     */
    boolean register(User user, List<Long> roleIds);
    
    /**
     * 更新用户信息
     */
    boolean updateUser(User user);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
    
    /**
     * 为用户分配角?
     */
    boolean assignRolesToUser(Long userId, List<Long> roleIds);
    
    /**
     * 为角色分配权?
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);
}
