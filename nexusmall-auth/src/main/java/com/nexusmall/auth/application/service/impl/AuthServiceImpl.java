package com.nexusmall.auth.application.service.impl;

import com.nexusmall.auth.infrastructure.persistence.dao.PermissionMapper;
import com.nexusmall.auth.infrastructure.persistence.dao.RoleMapper;
import com.nexusmall.auth.infrastructure.persistence.dao.UserMapper;
import com.nexusmall.auth.domain.entity.Permission;
import com.nexusmall.auth.domain.entity.Role;
import com.nexusmall.auth.domain.entity.User;
import com.nexusmall.auth.infrastructure.messaging.UserRegisteredEvent;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.exception.AuthException;
import com.nexusmall.auth.application.service.AuthService;
import com.nexusmall.auth.application.service.RefreshTokenService;
import com.nexusmall.auth.application.service.TokenBlacklistService;
import com.nexusmall.auth.util.JwtUtil;
import com.nexusmall.auth.interfaces.dto.AuthRequest;
import com.nexusmall.auth.interfaces.dto.AuthResponse;
import com.nexusmall.common.constant.ErrorMessageConstants;
import com.nexusmall.common.enums.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TokenBlacklistService blacklistService;
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public AuthResponse login(AuthRequest request) {
        log.info("用户登录，username: {}", request.getUsername());
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || user.getStatus() != 1) {
            log.warn("用户不存在或已禁用，username: {}", request.getUsername());
            throw new AuthException(ResultCode.INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("密码不匹配，username: {}", request.getUsername());
            throw new AuthException(ResultCode.INVALID_CREDENTIALS);
        }

        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<Permission> permissions = permissionMapper.findByUserId(user.getId());

        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());

        // 生成 Access Token (RS256)
        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), roleCodes, permissionCodes);
        
        // 生成 Refresh Token
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        String refreshJti = jwtUtil.getJtiFromToken(refreshToken);
        
        // 保存 Refresh Token 到数据库
        refreshTokenService.saveRefreshToken(user, refreshToken, refreshJti, null, null);
        
        log.info("用户登录成功，username: {}, roles: {}, permissions: {}", 
                user.getUsername(), roleCodes.size(), permissionCodes.size());

        AuthResponse response = new AuthResponse();
        response.setToken(accessToken);
        response.setRefreshToken(refreshToken); // 新增
        response.setExpireTime(System.currentTimeMillis() + 1800000L); // 30分钟
        response.setUsername(user.getUsername());
        response.setRoles(roleCodes);
        response.setPermissions(permissionCodes);
        return response;
    }

    @Override
    public void logout(String token) {
        log.info("用户登出");
        
        try {
            // 1. 获取 Token JTI
            String jti = jwtUtil.getJtiFromToken(token);
            
            // 2. 获取用户?
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userMapper.findByUsername(username);
            
            if (user != null) {
                // 3. 撤销该用户的所?Refresh Token
                refreshTokenService.revokeAllRefreshTokens(user.getId());
            }
            
            // 4. ?Access Token 加入黑名?
            long remainingTime = jwtUtil.validateToken(token) ? 
                    calculateRemainingTime(token) : 0;
            if (remainingTime > 0) {
                blacklistService.addToBlacklist(jti, remainingTime);
            }
            
            log.info("用户登出成功，username: {}", username);
        } catch (Exception e) {
            log.error("用户登出失败", e);
        }
    }
    
    /**
     * 计算 Token 剩余有效?
     */
    private long calculateRemainingTime(String token) {
        try {
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            long expirationTime = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();
            return Math.max(0, expirationTime - currentTime);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 获取 RSA 公钥 (用于验证)
     */
    private java.security.PublicKey getPublicKey() {
        try {
            String publicKeyPEM = ""; // TODO: ?RsaKeyProperties 获取
            publicKeyPEM = publicKeyPEM
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = java.util.Base64.getDecoder().decode(publicKeyPEM);
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("加载 RSA 公钥失败", e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("验证 Token: {}...", token.length() > 20 ? token.substring(0, 20) : token);
        
        // 1. 检查是否在黑名单中
        String jti = jwtUtil.getJtiFromToken(token);
        if (blacklistService.isBlacklisted(jti)) {
            log.warn("Token 已在黑名单中，jti: {}", jti);
            return false;
        }
        
        // 2. 验证 Token 签名和过期时?
        boolean valid = jwtUtil.validateToken(token);
        log.info("Token 验证{}", valid ? "成功" : "失败");
        return valid;
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        log.info("刷新 Access Token");
        return refreshTokenService.refreshAccessToken(refreshToken);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user, List<Long> roleIds) {
        log.info("开始注册用户，username: {}", user.getUsername());
        
        // 1. 检查用户名是否已存?
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new AuthException(ResultCode.USER_ALREADY_EXISTS);
        }
        
        // 2. 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1); // 默认启用
        
        // 3. 插入用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new AuthException(ResultCode.USER_REGISTRATION_FAILED);
        }
        
        // 4. 分配角色（如果有?
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                com.nexusmall.auth.domain.entity.UserRole userRole = new com.nexusmall.auth.domain.entity.UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userMapper.insertUserRole(userRole);
            }
        } else {
            // 5. 如果没有指定角色,自动分配 CUSTOMER 角色
            Role customerRole = roleMapper.findByRoleCode("CUSTOMER");
            if (customerRole != null) {
                com.nexusmall.auth.domain.entity.UserRole userRole = new com.nexusmall.auth.domain.entity.UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(customerRole.getId());
                userMapper.insertUserRole(userRole);
                log.info("自动分配 CUSTOMER 角色给用户，userId: {}", user.getId());
            }
        }
        
        // 6. 发送用户注册事件到 RocketMQ（异步创建会员档案）
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .email(user.getEmail())
                .timestamp(System.currentTimeMillis())
                .build();
            
            rocketMQTemplate.convertAndSend("USER_REGISTERED_TOPIC", event);
            log.info("用户注册事件已发送，userId: {}", user.getId());
        } catch (Exception e) {
            log.error("发送用户注册事件失败，userId: {}", user.getId(), e);
            // 注意：这里不抛出异常，避免影响注册流?
            // Member 服务会通过重试机制最终处?
        }
        
        log.info("用户注册成功，userId: {}", user.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User user) {
        log.info("【管理操作】更新用户信息，userId: {}", user.getId());
        
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND);
        }
        
        // 生产级：密码修改需要特殊处?
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            // 只有明确提供了新密码才修?
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            log.info("用户密码已更新，userId: {}", user.getId());
        } else {
            // 不修改密码字?
            user.setPassword(null);
        }
        
        int result = userMapper.updateById(user);
        log.info("【管理操作】用户信息更新{}，userId: {}", result > 0 ? "成功" : "失败", user.getId());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        log.info("【管理操作】删除用户，userId: {}", userId);
        
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND);
        }
        
        // 生产级：逻辑删除（软删除），保留审计追踪
        existingUser.setStatus(0); // 0=禁用（逻辑删除?
        int result = userMapper.updateById(existingUser);
        
        // 撤销该用户的所?Token
        try {
            refreshTokenService.revokeAllRefreshTokens(userId);
            log.info("已撤销用户所?Token，userId: {}", userId);
        } catch (Exception e) {
            log.error("撤销用户 Token 失败，userId: {}", userId, e);
            // 不抛出异常，避免影响主流?
        }
        
        log.info("【管理操作】用户已逻辑删除，userId: {}, username: {}", userId, existingUser.getUsername());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("为用户分配角色，userId: {}, roleIds: {}", userId, roleIds);
        
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new AuthException(ResultCode.USER_NOT_FOUND);
        }
        
        // 删除原有的角色关?
        userMapper.deleteUserRoles(userId);
        
        // 添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                Role role = roleMapper.selectById(roleId);
                if (role == null) {
                    throw new AuthException(ResultCode.ROLE_NOT_FOUND, 
                            ErrorMessageConstants.Auth.ROLE_NOT_FOUND_WITH_ID + roleId);
                }
                com.nexusmall.auth.domain.entity.UserRole userRole = new com.nexusmall.auth.domain.entity.UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userMapper.insertUserRole(userRole);
            }
        }
        
        log.info("用户角色分配成功");
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        log.info("为角色分配权限，roleId: {}, permissionIds: {}", roleId, permissionIds);
        
        Role existingRole = roleMapper.selectById(roleId);
        if (existingRole == null) {
            throw new AuthException(ResultCode.ROLE_NOT_FOUND);
        }
        
        // 删除原有的权限关?
        roleMapper.deleteRolePermissions(roleId);
        
        // 添加新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                Permission permission = permissionMapper.selectById(permissionId);
                if (permission == null) {
                    throw new AuthException(ResultCode.PERMISSION_NOT_FOUND, 
                            ErrorMessageConstants.Auth.PERMISSION_NOT_FOUND_WITH_ID + permissionId);
                }
                com.nexusmall.auth.domain.entity.RolePermission rolePermission = new com.nexusmall.auth.domain.entity.RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                roleMapper.insertRolePermission(rolePermission);
            }
        }
        
        log.info("角色权限分配成功");
        return true;
    }
}
