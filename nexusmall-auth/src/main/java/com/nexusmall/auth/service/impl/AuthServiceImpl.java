package com.nexusmall.auth.service.impl;

import com.nexusmall.auth.dao.PermissionMapper;
import com.nexusmall.auth.dao.RoleMapper;
import com.nexusmall.auth.dao.UserMapper;
import com.nexusmall.auth.entity.Permission;
import com.nexusmall.auth.entity.Role;
import com.nexusmall.auth.entity.User;
import com.nexusmall.auth.exception.AuthException;
import com.nexusmall.auth.service.AuthService;
import com.nexusmall.auth.util.JwtUtil;
import com.nexusmall.auth.vo.AuthRequest;
import com.nexusmall.auth.vo.AuthResponse;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public AuthResponse login(AuthRequest request) {
        log.info("用户登录，username: {}", request.getUsername());
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || user.getStatus() != 1) {
            log.warn("用户不存在或已禁用，username: {}", request.getUsername());
            throw new AuthException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("密码不匹配，username: {}", request.getUsername());
            throw new AuthException("用户名或密码错误");
        }

        List<Role> roles = roleMapper.findByUserId(user.getId());
        List<Permission> permissions = permissionMapper.findByUserId(user.getId());

        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        List<String> permissionCodes = permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(user.getUsername(), roleCodes, permissionCodes);
        
        log.info("用户登录成功，username: {}, roles: {}, permissions: {}", 
                user.getUsername(), roleCodes.size(), permissionCodes.size());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setExpireTime(System.currentTimeMillis() + 86400000);
        response.setUsername(user.getUsername());
        response.setRoles(roleCodes);
        response.setPermissions(permissionCodes);
        return response;
    }

    @Override
    public void logout(String token) {
        log.info("用户登出，token: {}...", token.length() > 20 ? token.substring(0, 20) : token);
        // TODO: 可以将 token 加入黑名单（使用 Redis）
        log.debug("用户登出处理完成");
    }

    @Override
    public boolean validateToken(String token) {
        log.debug("验证 Token: {}...", token.length() > 20 ? token.substring(0, 20) : token);
        boolean valid = jwtUtil.validateToken(token);
        log.info("Token 验证{}", valid ? "成功" : "失败");
        return valid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(User user, List<Long> roleIds) {
        log.info("开始注册用户，username: {}", user.getUsername());
        
        // 1. 检查用户名是否已存在
        User existingUser = userMapper.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new AuthException("用户名已存在");
        }
        
        // 2. 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1); // 默认启用
        
        // 3. 插入用户
        int result = userMapper.insert(user);
        if (result <= 0) {
            throw new AuthException("用户注册失败");
        }
        
        // 4. 分配角色（如果有）
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                com.nexusmall.auth.entity.UserRole userRole = new com.nexusmall.auth.entity.UserRole();
                userRole.setUserId(user.getId());
                userRole.setRoleId(roleId);
                userMapper.insertUserRole(userRole);
            }
        }
        
        log.info("用户注册成功，userId: {}", user.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User user) {
        log.info("更新用户信息，userId: {}", user.getId());
        
        User existingUser = userMapper.selectById(user.getId());
        if (existingUser == null) {
            throw new AuthException("用户不存在");
        }
        
        // 如果修改了密码，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不修改密码
        }
        
        int result = userMapper.updateById(user);
        log.info("用户信息更新{}", result > 0 ? "成功" : "失败");
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        log.info("删除用户，userId: {}", userId);
        
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new AuthException("用户不存在");
        }
        
        // 先删除用户角色关联
        userMapper.deleteUserRoles(userId);
        
        // 再删除用户
        int result = userMapper.deleteById(userId);
        log.info("用户删除{}", result > 0 ? "成功" : "失败");
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        log.info("为用户分配角色，userId: {}, roleIds: {}", userId, roleIds);
        
        User existingUser = userMapper.selectById(userId);
        if (existingUser == null) {
            throw new AuthException("用户不存在");
        }
        
        // 删除原有的角色关联
        userMapper.deleteUserRoles(userId);
        
        // 添加新的角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                Role role = roleMapper.selectById(roleId);
                if (role == null) {
                    throw new AuthException("角色不存在：" + roleId);
                }
                com.nexusmall.auth.entity.UserRole userRole = new com.nexusmall.auth.entity.UserRole();
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
            throw new AuthException("角色不存在");
        }
        
        // 删除原有的权限关联
        roleMapper.deleteRolePermissions(roleId);
        
        // 添加新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                Permission permission = permissionMapper.selectById(permissionId);
                if (permission == null) {
                    throw new AuthException("权限不存在：" + permissionId);
                }
                com.nexusmall.auth.entity.RolePermission rolePermission = new com.nexusmall.auth.entity.RolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                roleMapper.insertRolePermission(rolePermission);
            }
        }
        
        log.info("角色权限分配成功");
        return true;
    }
}
