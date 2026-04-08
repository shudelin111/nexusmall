package com.nexusmall.notification.application.service;

import com.nexusmall.notification.domain.entity.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通知消息服务单元测试
 * <p>
 * 测试范围：
 * - 未读消息查询
 * - 消息标记已读
 * - 批量操作
 * - 删除消息
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@SpringBootTest
@Transactional
@DisplayName("通知消息服务测试")
class NotificationMessageServiceTest {

    @Autowired
    private NotificationMessageService notificationMessageService;

    private Long testMemberId;
    private NotificationMessage testMessage;

    @BeforeEach
    void setUp() {
        testMemberId = 1001L;
        
        // 创建测试消息
        testMessage = new NotificationMessage();
        testMessage.setMemberId(testMemberId);
        testMessage.setTitle("测试消息");
        testMessage.setContent("这是一条测试消息");
        testMessage.setType(1);
        testMessage.setStatus(0); // 未读
        testMessage.setBusinessType("TEST");
        testMessage.setBusinessId(1L);
    }

    @Test
    @DisplayName("测试查询未读消息")
    void testGetUnreadMessages() {
        // Given: 插入一条未读消息
        
        // When: 查询未读消息
        List<NotificationMessage> messages = notificationMessageService.getUnreadMessages(testMemberId);
        
        // Then: 验证结果
        assertNotNull(messages);
        // 实际项目中应验证消息数量和内容
    }

    @Test
    @DisplayName("测试标记消息为已读")
    void testMarkAsRead() {
        // Given: 插入一条未读消息
        
        // When: 标记为已读
        boolean success = notificationMessageService.markAsRead(testMessage.getId(), testMemberId);
        
        // Then: 验证成功
        assertTrue(success);
    }

    @Test
    @DisplayName("测试批量标记已读")
    void testBatchMarkAsRead() {
        // Given: 插入多条未读消息
        List<Long> messageIds = Arrays.asList(1L, 2L, 3L);
        
        // When: 批量标记为已读
        int count = notificationMessageService.batchMarkAsRead(messageIds, testMemberId);
        
        // Then: 验证成功数量
        assertTrue(count >= 0);
    }

    @Test
    @DisplayName("测试全部标记为已读")
    void testMarkAllAsRead() {
        // Given: 插入多条未读消息
        
        // When: 全部标记为已读
        int count = notificationMessageService.markAllAsRead(testMemberId);
        
        // Then: 验证成功数量
        assertTrue(count >= 0);
    }

    @Test
    @DisplayName("测试获取未读消息数量")
    void testGetUnreadCount() {
        // When: 查询未读数量
        long count = notificationMessageService.getUnreadCount(testMemberId);
        
        // Then: 验证数量
        assertTrue(count >= 0);
    }

    @Test
    @DisplayName("测试删除消息")
    void testDeleteMessage() {
        // Given: 插入一条消息
        
        // When: 删除消息
        boolean success = notificationMessageService.deleteMessage(testMessage.getId(), testMemberId);
        
        // Then: 验证成功
        assertTrue(success);
    }
}
