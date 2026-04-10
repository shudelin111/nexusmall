package com.nexusmall.auth.interfaces.controller;

import com.nexusmall.auth.application.service.AuthService;
import com.nexusmall.auth.domain.entity.User;
import com.nexusmall.auth.infrastructure.audit.AdminAuditLog;
import com.nexusmall.auth.infrastructure.audit.AdminAuditService;
import com.nexusmall.auth.interfaces.dto.AuthRequest;
import com.nexusmall.auth.interfaces.dto.AuthResponse;
import com.nexusmall.auth.interfaces.dto.RegisterRequest;
import com.nexusmall.auth.interfaces.dto.UserUpdateRequest;
import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.enums.ResultCode;
import com.nexusmall.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/")  // Gateway 已通过 /auth/** 路由，此处不需要再加前缀
@ApiVersion("v1")  // 标记此 Controller 支持 v1 版本
public class AuthController {

    private final AuthService authService;
    private final AdminAuditService auditService;

    public AuthController(AuthService authService, AdminAuditService auditService) {
        this.authService = authService;
        this.auditService = auditService;
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

    @PostMapping(value = "/login", headers = "X-API-Version=v1")
    public Result<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("收到登录请求，username: {}", request.getUsername());

        AuthResponse response = authService.login(request);
        log.info("用户登录成功，username: {}", request.getUsername());
        return Result.success(response);
    }

    @PostMapping(value = "/logout", headers = "X-API-Version=v1")
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

    @GetMapping(value = "/validate", headers = "X-API-Version=v1")
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
    @PostMapping(value = "/refresh", headers = "X-API-Version=v1")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Refresh Token 不能为空");
            return Result.failure(ResultCode.PARAM_INVALID);
        }

        log.info("收到刷新 Token 请求");
        String newAccessToken = authService.refreshAccessToken(refreshToken);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("expireTime", System.currentTimeMillis() + 1800000L); // 30分钟

        log.info("Token 刷新成功");
        return Result.success(response);
    }

    // ==================== 管理后台接口 ====================

    /**
     * 用户注册（管理后台）
     *
     * @param request 注册请求
     * @return 是否成功
     */
    @Operation(
        summary = "用户注册",
        description = "管理后台：创建新用户，支持分配角色"
    )
    @PostMapping(value = "/register", headers = "X-API-Version=v1")
    public Result<Boolean> register(@Validated @RequestBody RegisterRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("【管理操作】注册用户: {}", request.getUsername());

        try {
            User user = new User();
            BeanUtils.copyProperties(request, user);

            boolean success = authService.register(user, request.getRoleIds());

            // 记录审计日志
            AdminAuditLog auditLog = auditService.createSuccessLog(
                "REGISTER_USER",
                "admin", // TODO: 从 SecurityContext 获取当前用户
                null,
                user.getId(),
                "USER",
                String.format("注册用户: %s", request.getUsername()),
                null, // TODO: 从 Request 获取 IP
                null,
                startTime
            );
            auditService.logAudit(auditLog);

            return Result.success(success);
        } catch (Exception e) {
            // 记录失败审计日志
            AdminAuditLog auditLog = auditService.createFailedLog(
                "REGISTER_USER",
                "admin",
                null,
                null,
                "USER",
                String.format("注册用户: %s", request.getUsername()),
                e.getMessage(),
                null,
                null,
                startTime
            );
            auditService.logAudit(auditLog);

            throw e;
        }
    }

    /**
     * 更新用户信息
     *
     * @param userId 用户 ID
     * @param request 更新请求
     * @return 是否成功
     */
    @Operation(
        summary = "更新用户信息",
        description = "管理后台：更新邮箱、手机号、状态等"
    )
    @PutMapping(value = "/users/{userId}", headers = "X-API-Version=v1")
    public Result<Boolean> updateUser(
            @PathVariable Long userId,
            @Validated @RequestBody UserUpdateRequest request) {
        log.info("管理后台更新用户信息: userId={}", userId);

        User user = new User();
        user.setId(userId);
        BeanUtils.copyProperties(request, user);

        // 转换 status 为 Integer
        if (request.getStatus() != null) {
            user.setStatus(Integer.parseInt(request.getStatus()));
        }

        boolean success = authService.updateUser(user);
        return Result.success(success);
    }

    /**
     * 删除用户
     *
     * @param userId 用户 ID
     * @return 是否成功
     */
    @Operation(
        summary = "删除用户",
        description = "管理后台：逻辑删除用户（软删除）"
    )
    @DeleteMapping(value = "/users/{userId}", headers = "X-API-Version=v1")
    public Result<Boolean> deleteUser(@PathVariable Long userId) {
        long startTime = System.currentTimeMillis();
        log.info("【管理操作】删除用户: userId={}", userId);

        try {
            boolean success = authService.deleteUser(userId);

            // 记录审计日志
            AdminAuditLog auditLog = auditService.createSuccessLog(
                "DELETE_USER",
                "admin",
                null,
                userId,
                "USER",
                String.format("逻辑删除用户: userId=%d", userId),
                null,
                null,
                startTime
            );
            auditService.logAudit(auditLog);

            return Result.success(success);
        } catch (Exception e) {
            AdminAuditLog auditLog = auditService.createFailedLog(
                "DELETE_USER",
                "admin",
                null,
                userId,
                "USER",
                String.format("逻辑删除用户: userId=%d", userId),
                e.getMessage(),
                null,
                null,
                startTime
            );
            auditService.logAudit(auditLog);

            throw e;
        }
    }

    /**
     * 为用户分配角色
     *
     * @param userId 用户 ID
     * @param roleIds 角色 ID 列表
     * @return 是否成功
     */
    @Operation(
        summary = "分配角色",
        description = "管理后台：为用户批量分配角色"
    )
    @PostMapping(value = "/users/{userId}/roles", headers = "X-API-Version=v1")
    public Result<Boolean> assignRolesToUser(
            @PathVariable Long userId,
            @RequestBody java.util.List<Long> roleIds) {
        log.info("管理后台为用户分配角色: userId={}, roleCount={}", userId, roleIds.size());
        boolean success = authService.assignRolesToUser(userId, roleIds);
        return Result.success(success);
    }

    /**
     * 为角色分配权限
     *
     * @param roleId 角色 ID
     * @param permissionIds 权限 ID 列表
     * @return 是否成功
     */
    @Operation(
        summary = "分配权限",
        description = "管理后台：为角色批量分配权限"
    )
    @PostMapping(value = "/roles/{roleId}/permissions", headers = "X-API-Version=v1")
    public Result<Boolean> assignPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody java.util.List<Long> permissionIds) {
        log.info("管理后台为角色分配权限: roleId={}, permissionCount={}", roleId, permissionIds.size());
        boolean success = authService.assignPermissionsToRole(roleId, permissionIds);
        return Result.success(success);
    }
}
