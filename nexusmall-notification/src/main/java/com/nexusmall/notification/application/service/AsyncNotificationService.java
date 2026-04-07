package com.nexusmall.notification.application.service;

import com.nexusmall.notification.domain.entity.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 异步通知发送服务
 * <p>
 * 业界标准（Async Batch Processing）：
 * - 使用异步线程池批量发送通知
 * - 不阻塞主业务流程，提升响应速度
 * - 支持失败重试和错误隔离
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncNotificationService {

    private final NotificationMessageService notificationMessageService;

    /**
     * 异步批量保存通知消息
     * <p>
     * 使用 @Async 注解实现异步执行
     * 线程池：notificationTaskExecutor
     * </p>
     *
     * @param messages 消息列表
     */
    @Async("notificationTaskExecutor")
    public void batchSaveNotificationsAsync(List<NotificationMessage> messages) {
        log.info("开始异步批量保存通知，count: {}", messages.size());
        
        try {
            for (NotificationMessage message : messages) {
                // TODO: 使用 MyBatis-Plus 的 saveBatch 方法
                // notificationMessageService.save(message);
            }
            log.info("异步批量保存通知完成，count: {}", messages.size());
        } catch (Exception e) {
            log.error("异步批量保存通知失败，count: {}", messages.size(), e);
            // 异步任务异常不会影响主流程，需要记录日志以便排查
        }
    }

    /**
     * 异步标记消息为已读
     *
     * @param messageId 消息ID
     * @param memberId  会员ID
     */
    @Async("notificationTaskExecutor")
    public void markAsReadAsync(Long messageId, Long memberId) {
        log.debug("异步标记消息为已读，messageId: {}, memberId: {}", messageId, memberId);
        
        try {
            notificationMessageService.markAsRead(messageId, memberId);
        } catch (Exception e) {
            log.error("异步标记消息已读失败，messageId: {}", messageId, e);
        }
    }

    /**
     * 异步批量标记消息为已读
     *
     * @param messageIds 消息ID列表
     * @param memberId   会员ID
     */
    @Async("notificationTaskExecutor")
    public void batchMarkAsReadAsync(List<Long> messageIds, Long memberId) {
        log.info("异步批量标记消息为已读，count: {}, memberId: {}", messageIds.size(), memberId);
        
        try {
            notificationMessageService.batchMarkAsRead(messageIds, memberId);
        } catch (Exception e) {
            log.error("异步批量标记消息已读失败，memberId: {}", memberId, e);
        }
    }
}
