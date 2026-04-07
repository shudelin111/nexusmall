package com.nexusmall.notification.application.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 幂等性服务单元测试
 * <p>
 * 测试范围：
 * - Redis SETNX 原子操作
 * - 过期时间设置
 * - 重复事件检测
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@SpringBootTest
@DisplayName("幂等性服务测试")
class IdempotencyServiceTest {

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String testEventId;

    @BeforeEach
    void setUp() {
        testEventId = "test-event-" + System.currentTimeMillis();
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        idempotencyService.removeProcessed(testEventId);
    }

    @Test
    @DisplayName("测试首次处理事件")
    void testTryProcess_FirstTime() {
        // When: 首次处理事件
        boolean result = idempotencyService.tryProcess(testEventId, 3600);
        
        // Then: 应该返回 true
        assertTrue(result);
    }

    @Test
    @DisplayName("测试重复处理事件")
    void testTryProcess_Duplicate() {
        // Given: 首次处理
        idempotencyService.tryProcess(testEventId, 3600);
        
        // When: 再次处理
        boolean result = idempotencyService.tryProcess(testEventId, 3600);
        
        // Then: 应该返回 false
        assertFalse(result);
    }

    @Test
    @DisplayName("测试检查事件是否已处理")
    void testIsProcessed() {
        // Given: 处理事件
        idempotencyService.tryProcess(testEventId, 3600);
        
        // When: 检查状态
        boolean isProcessed = idempotencyService.isProcessed(testEventId);
        
        // Then: 应该返回 true
        assertTrue(isProcessed);
    }

    @Test
    @DisplayName("测试检查未处理事件")
    void testIsProcessed_NotProcessed() {
        // When: 检查未处理事件
        boolean isProcessed = idempotencyService.isProcessed(testEventId);
        
        // Then: 应该返回 false
        assertFalse(isProcessed);
    }

    @Test
    @DisplayName("测试标记事件已处理")
    void testMarkProcessed() {
        // When: 标记事件已处理
        idempotencyService.markProcessed(testEventId, 3600);
        
        // Then: 检查状态
        assertTrue(idempotencyService.isProcessed(testEventId));
    }

    @Test
    @DisplayName("测试删除事件处理记录")
    void testRemoveProcessed() {
        // Given: 处理事件
        idempotencyService.tryProcess(testEventId, 3600);
        
        // When: 删除记录
        idempotencyService.removeProcessed(testEventId);
        
        // Then: 检查状态
        assertFalse(idempotencyService.isProcessed(testEventId));
    }
}
