package com.nexusmall.promotion.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.promotion.domain.entity.Notification;
import com.nexusmall.promotion.application.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息通知控制器（RESTful标准版）
 * <p>
 * RESTful资源设计?
 * - GET    /notifications/unread          - 查询未读消息列表
 * - POST   /notifications/{id}/read       - 标记单条消息为已?
 * - PATCH  /notifications/read-all        - 全部标记为已?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/notifications")  // RESTful资源路径：消息通知集合
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "消息通知", description = "站内信、短信、邮件等通知管理")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 查询未读消息列表
     *
     * @param userId 用户ID
     * @return 未读消息列表
     */
    @GetMapping(value = "/unread", headers = "X-API-Version=v1")
    @Operation(summary = "查询未读消息列表", description = "获取当前用户的所有未读消息")
    public Result<List<Notification>> listUnreadNotifications(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        log.info("【查询未读消息】userId={}", userId);
        List<Notification> notifications = notificationService.listUnreadNotifications(userId);
        return Result.success(notifications);
    }

    /**
     * 标记单条消息为已读
     *
     * @param id     消息ID
     * @param userId 用户ID
     * @return 是否成功
     */
    @PostMapping(value = "/{id}/read", headers = "X-API-Version=v1")
    @Operation(summary = "标记消息为已读", description = "将指定消息标记为已读状态")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        log.info("【标记已读】id={}, userId={}", id, userId);
        boolean success = notificationService.markAsRead(id, userId);
        return success ? Result.success() : Result.failure("500", "操作失败");
    }

    /**
     * 全部标记为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @PatchMapping(value = "/read-all", headers = "X-API-Version=v1")
    @Operation(summary = "全部标记为已读", description = "将当前用户的所有未读消息标记为已读")
    public Result<Integer> markAllAsRead(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader("X-User-ID") Long userId) {
        log.info("【全部标记已读】userId={}", userId);
        int count = notificationService.markAllAsRead(userId);
        return Result.success(count);
    }
}
