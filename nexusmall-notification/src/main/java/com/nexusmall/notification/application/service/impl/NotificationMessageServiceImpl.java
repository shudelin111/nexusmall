package com.nexusmall.notification.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nexusmall.notification.application.service.NotificationMessageService;
import com.nexusmall.notification.domain.entity.NotificationMessage;
import com.nexusmall.notification.infrastructure.persistence.mapper.NotificationMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知消息应用服务实现
 * <p>
 * 业界标准（Application Service Pattern）：
 * - 实现领域服务的业务逻辑编排
 * - 提供事务管理和异常处理
 * - 符合单一职责原则
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationMessageServiceImpl implements NotificationMessageService {

    private final NotificationMessageMapper notificationMessageMapper;

    @Override
    public List<NotificationMessage> getUnreadMessages(Long memberId) {
        log.debug("查询未读消息，memberId: {}", memberId);
        
        LambdaQueryWrapper<NotificationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationMessage::getMemberId, memberId)
               .eq(NotificationMessage::getStatus, 0) // 0=未读
               .orderByDesc(NotificationMessage::getCreateTime);
        
        return notificationMessageMapper.selectList(wrapper);
    }

    @Override
    public List<NotificationMessage> getMessageList(Long memberId, Integer pageNum, Integer pageSize) {
        log.debug("查询消息列表，memberId: {}, pageNum: {}, pageSize: {}", memberId, pageNum, pageSize);
        
        Page<NotificationMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<NotificationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationMessage::getMemberId, memberId)
               .orderByDesc(NotificationMessage::getCreateTime);
        
        Page<NotificationMessage> result = notificationMessageMapper.selectPage(page, wrapper);
        return result.getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markAsRead(Long messageId, Long memberId) {
        log.info("标记消息为已读，messageId: {}, memberId: {}", messageId, memberId);
        
        LambdaUpdateWrapper<NotificationMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotificationMessage::getId, messageId)
               .eq(NotificationMessage::getMemberId, memberId) // 权限校验
               .set(NotificationMessage::getStatus, 1); // 1=已读
        
        int rows = notificationMessageMapper.update(null, wrapper);
        
        if (rows > 0) {
            log.info("消息标记已读成功，messageId: {}", messageId);
            return true;
        } else {
            log.warn("消息标记已读失败，可能不存在或无权限，messageId: {}, memberId: {}", messageId, memberId);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkAsRead(List<Long> messageIds, Long memberId) {
        log.info("批量标记消息为已读，count: {}, memberId: {}", messageIds.size(), memberId);
        
        LambdaUpdateWrapper<NotificationMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(NotificationMessage::getId, messageIds)
               .eq(NotificationMessage::getMemberId, memberId) // 权限校验
               .set(NotificationMessage::getStatus, 1);
        
        int rows = notificationMessageMapper.update(null, wrapper);
        log.info("批量标记已读完成，成功: {} 条", rows);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAllAsRead(Long memberId) {
        log.info("全部标记为已读，memberId: {}", memberId);
        
        LambdaUpdateWrapper<NotificationMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotificationMessage::getMemberId, memberId)
               .eq(NotificationMessage::getStatus, 0) // 只更新未读消息
               .set(NotificationMessage::getStatus, 1);
        
        int rows = notificationMessageMapper.update(null, wrapper);
        log.info("全部标记已读完成，成功: {} 条", rows);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long messageId, Long memberId) {
        log.info("删除消息，messageId: {}, memberId: {}", messageId, memberId);
        
        // MyBatis-Plus 的逻辑删除会自动处理 @TableLogic 注解
        int rows = notificationMessageMapper.deleteById(messageId);
        
        if (rows > 0) {
            log.info("消息删除成功，messageId: {}", messageId);
            return true;
        } else {
            log.warn("消息删除失败，可能不存在或无权限，messageId: {}, memberId: {}", messageId, memberId);
            return false;
        }
    }

    @Override
    public long getUnreadCount(Long memberId) {
        log.debug("查询未读消息数量，memberId: {}", memberId);
        
        LambdaQueryWrapper<NotificationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationMessage::getMemberId, memberId)
               .eq(NotificationMessage::getStatus, 0); // 0=未读
        
        return notificationMessageMapper.selectCount(wrapper);
    }
}
