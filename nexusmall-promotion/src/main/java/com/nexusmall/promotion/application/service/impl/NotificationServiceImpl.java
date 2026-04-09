package com.nexusmall.promotion.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.promotion.application.service.NotificationService;
import com.nexusmall.promotion.domain.entity.Notification;
import com.nexusmall.promotion.infrastructure.persistence.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息通知服务实现类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendInboxNotification(Long userId, Integer type, String title, String content) {
        log.info("发送站内信通知: userId={}, type={}, title={}", userId, type, title);
        
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(0);  // 未读
        notification.setCreateTime(LocalDateTime.now());
        
        boolean success = this.save(notification);
        
        if (success) {
            log.info("站内信发送成功 notificationId={}, userId={}", notification.getId(), userId);
        } else {
            log.error("站内信发送失败 userId={}", userId);
        }
        
        return success;
    }

    @Override
    public List<Notification> listUnreadNotifications(Long userId) {
        log.info("查询用户未读消息列表: userId={}", userId);
        
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)  // 未读
               .orderByDesc(Notification::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long notificationId, Long userId) {
        log.info("标记消息为已读 notificationId={}, userId={}", notificationId, userId);
        
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, notificationId)
               .eq(Notification::getUserId, userId)
               .set(Notification::getIsRead, 1)
               .set(Notification::getReadTime, LocalDateTime.now());
        
        boolean success = this.update(wrapper);
        
        if (success) {
            log.info("消息标记为已读成功 notificationId={}", notificationId);
        } else {
            log.warn("消息标记为已读失败 notificationId={}", notificationId);
        }
        
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead(Long userId) {
        log.info("批量标记为已读 userId={}", userId);
        
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .eq(Notification::getIsRead, 0)  // 只更新未读的
               .set(Notification::getIsRead, 1)
               .set(Notification::getReadTime, LocalDateTime.now());
        
        int count = this.getBaseMapper().update(null, wrapper);
        
        log.info("批量标记为已读完成 userId={}, 影响行数={}", userId, count);
        
        return count;
    }
}
