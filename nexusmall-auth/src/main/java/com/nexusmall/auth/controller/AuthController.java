package com.nexusmall.auth.controller;

import com.nexusmall.auth.service.AuthService;
import com.nexusmall.auth.vo.AuthRequest;
import com.nexusmall.auth.vo.AuthResponse;
import com.nexusmall.common.vo.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody AuthRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            authService.logout(token);
        }
        return Result.success();
    }

    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return Result.success(authService.validateToken(token));
        }
        return Result.success(false);
    }
}
