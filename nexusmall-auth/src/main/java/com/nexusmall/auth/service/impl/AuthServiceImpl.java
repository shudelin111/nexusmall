package com.nexusmall.auth.service.impl;

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

    private final User mockUser = User.builder()
            .id(1L)
            .username("admin")
            .password("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG")
            .email("admin@nexusmall.com")
            .status(1)
            .build();

    @Override
    public AuthResponse login(AuthRequest request) {
        if (!mockUser.getUsername().equals(request.getUsername())
                || !passwordEncoder.matches(request.getPassword(), mockUser.getPassword())) {
            throw new AuthException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(mockUser.getUsername());

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setExpireTime(System.currentTimeMillis() + 86400000);
        response.setUsername(mockUser.getUsername());
        return response;
    }

    @Override
    public void logout(String token) {
    }

    @Override
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
}
