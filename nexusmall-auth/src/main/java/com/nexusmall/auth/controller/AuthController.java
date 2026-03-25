package com.nexusmall.auth.controller;

import com.nexusmall.auth.service.AuthService;
import com.nexusmall.auth.vo.AuthRequest;
import com.nexusmall.auth.vo.AuthResponse;
import com.nexusmall.common.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("service", "nexusmall-auth");
        payload.put("status", "UP");
        payload.put("message", "auth service is ready");
        return Result.success(payload);
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("收到登录请求，username: {}", request.getUsername());
        try {
            AuthResponse response = authService.login(request);
            log.info("用户登录成功，username: {}", request.getUsername());
            return Result.success(response);
        } catch (Exception e) {
            log.error("用户登录失败，username: {}, 错误：{}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.info("用户登出，token 已处理");
            authService.logout(token);
        } else {
            log.warn("无效的 Token 格式");
        }
        return Result.success();
    }

    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.debug("验证 Token: {}", token.substring(0, Math.min(20, token.length())) + "...");
            boolean valid = authService.validateToken(token);
            log.info("Token 验证结果：{}", valid ? "有效" : "无效");
            return Result.success(valid);
        }
        log.warn("无效的 Token 格式");
        return Result.success(false);
    }
}
