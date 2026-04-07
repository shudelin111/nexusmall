package com.nexusmall.promotion.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.promotion.domain.entity.Notification;

import java.util.List;

/**
 * 消息通知 Service 接口
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送站内信通知
     *
     * @param userId  用户ID
     * @param type    通知类型
     * @param title   标题
     * @param content 内容
     * @return 是否成功
     */
    boolean sendInboxNotification(Long userId, Integer type, String title, String content);

    /**
     * 查询用户未读消息列表
     *
     * @param userId 用户ID
     * @return 未读消息列表
     */
    List<Notification> listUnreadNotifications(Long userId);

    /**
     * 标记消息为已读
     *
     * @param notificationId 消息ID
     * @param userId         用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 批量标记为已读
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int markAllAsRead(Long userId);
}
