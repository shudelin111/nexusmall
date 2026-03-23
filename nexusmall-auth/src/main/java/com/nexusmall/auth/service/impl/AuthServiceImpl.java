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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || user.getStatus() != 1) {
            throw new AuthException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
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
        // TODO: 可以将 token 加入黑名单（使用 Redis）
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
