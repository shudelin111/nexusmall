package com.nexusmall.auth.service;

import com.nexusmall.auth.vo.AuthRequest;
import com.nexusmall.auth.vo.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);
    void logout(String token);
    boolean validateToken(String token);
}
