package com.nexusmall.notification.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.notification.application.service.NotificationMessageService;
import com.nexusmall.notification.domain.entity.NotificationMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知消息控制器（RESTful标准版）
 * <p>
 * RESTful资源设计：
 * - GET    /api/v1/notifications/unread          - 查询未读消息列表
 * - GET    /api/v1/notifications/list            - 查询消息列表（分页）
 * - POST   /api/v1/notifications/{id}/read       - 标记单条消息为已读
 * - PATCH  /api/v1/notifications/read-batch      - 批量标记为已读
 * - PATCH  /api/v1/notifications/read-all        - 全部标记为已读
 * - DELETE /api/v1/notifications/{id}            - 删除消息
 * - GET    /api/v1/notifications/unread-count    - 获取未读消息数量
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "通知消息", description = "站内消息管理接口")
public class NotificationMessageController {

    private final NotificationMessageService notificationMessageService;

    @GetMapping("/unread")
    @Operation(summary = "查询未读消息列表", description = "获取指定用户的所有未读消息")
    public Result<List<NotificationMessage>> getUnreadMessages(
            @Parameter(description = "会员ID", required = true) @RequestParam Long memberId) {
        log.info("查询未读消息，memberId: {}", memberId);
        List<NotificationMessage> messages = notificationMessageService.getUnreadMessages(memberId);
        return Result.success(messages);
    }

    @GetMapping("/list")
    @Operation(summary = "查询消息列表", description = "分页获取指定用户的消息列表")
    public Result<List<NotificationMessage>> getMessageList(
            @Parameter(description = "会员ID", required = true) @RequestParam Long memberId,
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("查询消息列表，memberId: {}, pageNum: {}, pageSize: {}", memberId, pageNum, pageSize);
        List<NotificationMessage> messages = notificationMessageService.getMessageList(memberId, pageNum, pageSize);
        return Result.success(messages);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "标记单条消息为已读", description = "将指定消息标记为已读状态")
    public Result<Boolean> markAsRead(
            @Parameter(description = "消息ID", required = true) @PathVariable Long id,
            @Parameter(description = "会员ID", required = true) @RequestParam Long memberId) {
        log.info("标记消息为已读，messageId: {}, memberId: {}", id, memberId);
        boolean success = notificationMessageService.markAsRead(id, memberId);
        return success ? Result.success(true) : Result.failure("MARK_READ_FAILED", "标记失败，消息不存在或无权限");
    }

    @PatchMapping("/read-batch")
    @Operation(summary = "批量标记为已读", description = "将多条消息批量标记为已读状态")
    public Result<Integer> batchMarkAsRead(
            @Parameter(description = "消息ID列表", required = true) @RequestBody List<Long> messageIds,
            @Parameter(description = "会员ID", required = true) @RequestParam Long memberId) {
        log.info("批量标记消息为已读，count: {}, memberId: {}", messageIds.size(), memberId);
        int count = notificationMessageService.batchMarkAsRead(messageIds, memberId);
        return Result.success(count);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "全部标记为已读", description = "将指定用户的所有未读消息标记为已读")
    public Result<Integer> markAllAsRead(
            @Parameter(description = "会员ID", required = true) @RequestParam Long memberId) {
        log.info("全部标记为已读，memberId: {}", memberId);
        int count = notificationMessageService.markAllAsRead(memberId);
        return Result.success(count);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "删除指定的消息（逻辑删除）")
    public Result<Boolean> deleteMessage(
            @Parameter(description = "消息ID", required = true) @PathVariable Long id,
            @Parameter(description = "会员ID", required = true) @RequestParam Long memberId) {
        log.info("删除消息，messageId: {}, memberId: {}", id, memberId);
        boolean success = notificationMessageService.deleteMessage(id, memberId);
        return success ? Result.success(true) : Result.failure("DELETE_FAILED", "删除失败，消息不存在或无权限");
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量", description = "获取指定用户的未读消息总数")
    public Result<Long> getUnreadCount(
            @Parameter(description = "会员ID", required = true) @RequestParam Long memberId) {
        log.info("查询未读消息数量，memberId: {}", memberId);
        long count = notificationMessageService.getUnreadCount(memberId);
        return Result.success(count);
    }
}
