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
@RequestMapping("/")  // Gateway 已通过 /auth/** 路由，此处不需要再加前缀
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
        
        AuthResponse response = authService.login(request);
        log.info("用户登录成功，username: {}", request.getUsername());
        return Result.success(response);
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

    /**
     * 刷新 Access Token
     *
     * @param request 包含 Refresh Token 的请求
     * @return 新的 Access Token
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh Token 不能为空");
            return Result.failure("400", "Refresh Token 不能为空");
        }
        
        log.info("收到刷新 Token 请求");
        String newAccessToken = authService.refreshAccessToken(refreshToken);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("expireTime", System.currentTimeMillis() + 1800000L); // 30分钟
        
        log.info("Token 刷新成功");
        return Result.success(response);
    }
}
