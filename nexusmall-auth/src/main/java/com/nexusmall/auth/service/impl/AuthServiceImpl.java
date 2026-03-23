package com.nexusmall.auth.service.impl;

import com.nexusmall.auth.dao.UserMapper;
import com.nexusmall.auth.entity.User;
import com.nexusmall.auth.exception.AuthException;
import com.nexusmall.auth.service.AuthService;
import com.nexusmall.auth.util.JwtUtil;
import com.nexusmall.auth.vo.AuthRequest;
import com.nexusmall.auth.vo.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || user.getStatus() != 1) {
            throw new AuthException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setExpireTime(System.currentTimeMillis() + 86400000);
        response.setUsername(user.getUsername());
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
