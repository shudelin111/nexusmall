package com.nexusmall.auth.service;

import com.nexusmall.auth.entity.Role;
import com.nexusmall.auth.entity.User;
import com.nexusmall.auth.vo.AuthRequest;
import com.nexusmall.auth.vo.AuthResponse;

import java.util.List;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    void logout(String token);
    boolean validateToken(String token);
    
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
     * 为用户分配角色
     */
    boolean assignRolesToUser(Long userId, List<Long> roleIds);
    
    /**
     * 为角色分配权限
     */
    boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds);
}
