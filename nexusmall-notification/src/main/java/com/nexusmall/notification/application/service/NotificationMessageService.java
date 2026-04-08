package com.nexusmall.notification.application.service;

import com.nexusmall.notification.domain.entity.NotificationMessage;

import java.util.List;

/**
 * 通知消息应用服务接口
 * <p>
 * 业界标准（CQRS + DDD）：
 * - 提供站内消息的查询和管理功能
 * - 支持按用户、类型、状态等多维度查询
 * - 符合命令查询职责分离原则
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface NotificationMessageService {

    /**
     * 查询用户的未读消息列表
     *
     * @param memberId 会员ID
     * @return 未读消息列表
     */
    List<NotificationMessage> getUnreadMessages(Long memberId);

    /**
     * 查询用户的所有消息列表（分页）
     *
     * @param memberId 会员ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 消息列表
     */
    List<NotificationMessage> getMessageList(Long memberId, Integer pageNum, Integer pageSize);

    /**
     * 标记单条消息为已读
     *
     * @param messageId 消息ID
     * @param memberId  会员ID（用于权限校验）
     * @return 是否成功
     */
    boolean markAsRead(Long messageId, Long memberId);

    /**
     * 批量标记消息为已读
     *
     * @param messageIds 消息ID列表
     * @param memberId   会员ID（用于权限校验）
     * @return 成功标记的数量
     */
    int batchMarkAsRead(List<Long> messageIds, Long memberId);

    /**
     * 全部标记为已读
     *
     * @param memberId 会员ID
     * @return 成功标记的数量
     */
    int markAllAsRead(Long memberId);

    /**
     * 删除消息
     *
     * @param messageId 消息ID
     * @param memberId  会员ID（用于权限校验）
     * @return 是否成功
     */
    boolean deleteMessage(Long messageId, Long memberId);

    /**
     * 获取未读消息数量
     *
     * @param memberId 会员ID
     * @return 未读消息数量
     */
    long getUnreadCount(Long memberId);
}
