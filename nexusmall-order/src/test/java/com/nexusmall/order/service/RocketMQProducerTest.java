package com.nexusmall.order.service;

import com.nexusmall.order.application.service.RocketMQProducer;
import com.nexusmall.common.constant.MQConstants;
import com.nexusmall.common.exception.OrderException;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RocketMQProducer 单元测试
 * <p>
 * 测试策略:
 * 1. 使用 @ExtendWith(MockitoExtension.class) 启用 Mockito
 * 2. 使用 @Mock 模拟 RocketMQTemplate 依赖
 * 3. 使用 @InjectMocks 自动注入 Mock 到被测对象
 * 4. 验证消息发送行为和异常处理
 * </p>
 *
 * @author shudl
 * @since 2026-04-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RocketMQ 消息生产者测试")
class RocketMQProducerTest {

    // ==================== Mock 依赖 ====================
    
    @Mock
    private RocketMQTemplate rocketMQTemplate;
    
    @InjectMocks
    private RocketMQProducer rocketMQProducer;
    
    // ==================== 测试数据准备 ====================
    
    private Long testOrderId;
    private int delayLevel;
    private String testTopic;
    private String testTag;
    private Object testPayload;
    
    @BeforeEach
    void setUp() {
        testOrderId = 1000L;
        delayLevel = 17; // 30分钟延迟
        testTopic = "TEST_TOPIC";
        testTag = "TEST_TAG";
        testPayload = "test message";
    }
    
    // ==================== sendOrderCancelDelayMessage 测试 ====================
    
    @Test
    @DisplayName("发送订单取消延迟消息 - 应成功调用 RocketMQTemplate")
    void sendOrderCancelDelayMessage_ShouldSendSuccessfully() {
        // Given: Mock RocketMQTemplate 的行为
        when(rocketMQTemplate.syncSend(anyString(), any(Message.class), anyLong(), anyInt()))
            .thenReturn(new org.apache.rocketmq.client.producer.SendResult());
        
        // When: 执行被测试方法
        rocketMQProducer.sendOrderCancelDelayMessage(testOrderId, delayLevel);
        
        // Then: 验证 RocketMQTemplate 被正确调用
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Message<?>> messageCaptor = ArgumentCaptor.forClass(Message.class);
        
        verify(rocketMQTemplate, times(1)).syncSend(
            destinationCaptor.capture(),
            messageCaptor.capture(),
            eq(3000L),
            eq(delayLevel)
        );
        
        // 验证目标地址格式正确
        String expectedDestination = MQConstants.Order.TOPIC + ":" + MQConstants.Order.CANCEL_TAG;
        assertThat(destinationCaptor.getValue()).isEqualTo(expectedDestination);
        
        // 验证消息体包含正确的 orderId
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo(testOrderId);
    }
    
    @Test
    @DisplayName("发送订单取消延迟消息 - RocketMQ 异常应抛出 OrderException")
    void sendOrderCancelDelayMessage_ShouldThrowOrderException_WhenRocketMQFails() {
        // Given: Mock RocketMQTemplate 抛出异常
        when(rocketMQTemplate.syncSend(anyString(), any(Message.class), anyLong(), anyInt()))
            .thenThrow(new RuntimeException("RocketMQ connection failed"));
        
        // When & Then: 验证抛出 OrderException
        assertThatThrownBy(() -> rocketMQProducer.sendOrderCancelDelayMessage(testOrderId, delayLevel))
            .isInstanceOf(OrderException.class)
            .hasMessageContaining("发送消息失败");
        
        // 验证 RocketMQTemplate 被调用过
        verify(rocketMQTemplate, times(1)).syncSend(anyString(), any(Message.class), anyLong(), anyInt());
    }
    
    @Test
    @DisplayName("发送订单取消延迟消息 - 不同延迟级别应正确传递")
    void sendOrderCancelDelayMessage_ShouldPassCorrectDelayLevel() {
        // Given
        when(rocketMQTemplate.syncSend(anyString(), any(Message.class), anyLong(), anyInt()))
            .thenReturn(new org.apache.rocketmq.client.producer.SendResult());
        
        // When: 使用不同的延迟级别
        int[] delayLevels = {1, 5, 10, 17, 18};
        for (int level : delayLevels) {
            rocketMQProducer.sendOrderCancelDelayMessage(testOrderId, level);
        }
        
        // Then: 验证 syncSend 被调用了 5 次
        verify(rocketMQTemplate, times(5)).syncSend(
            anyString(),
            any(Message.class),
            eq(3000L),
            anyInt()
        );
    }
    
    // ==================== sendNormalMessage 测试 ====================
    
    @Test
    @DisplayName("发送普通消息 - 应成功调用 RocketMQTemplate")
    void sendNormalMessage_ShouldSendSuccessfully() {
        // Given: Mock convertAndSend 不抛出异常
        doNothing().when(rocketMQTemplate).convertAndSend(anyString(), any(Object.class));
        
        // When: 执行被测试方法
        rocketMQProducer.sendNormalMessage(testTopic, testTag, testPayload);
        
        // Then: 验证 RocketMQTemplate 被正确调用
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        
        verify(rocketMQTemplate, times(1)).convertAndSend(
            destinationCaptor.capture(),
            payloadCaptor.capture()
        );
        
        // 验证目标地址格式正确
        String expectedDestination = testTopic + ":" + testTag;
        assertThat(destinationCaptor.getValue()).isEqualTo(expectedDestination);
        
        // 验证消息体正确
        assertThat(payloadCaptor.getValue()).isEqualTo(testPayload);
    }
    
    @Test
    @DisplayName("发送普通消息 - RocketMQ 异常应抛出 OrderException")
    void sendNormalMessage_ShouldThrowOrderException_WhenRocketMQFails() {
        // Given: Mock convertAndSend 抛出异常
        doThrow(new RuntimeException("Network error"))
            .when(rocketMQTemplate).convertAndSend(anyString(), any(Object.class));
        
        // When & Then: 验证抛出 OrderException
        assertThatThrownBy(() -> rocketMQProducer.sendNormalMessage(testTopic, testTag, testPayload))
            .isInstanceOf(OrderException.class)
            .hasMessageContaining("发送普通消息失败");
        
        // 验证 RocketMQTemplate 被调用过
        verify(rocketMQTemplate, times(1)).convertAndSend(anyString(), any(Object.class));
    }
    
    @Test
    @DisplayName("发送普通消息 - 不同 Topic 和 Tag 应正确拼接")
    void sendNormalMessage_ShouldConcatenateTopicAndTag() {
        // Given
        doNothing().when(rocketMQTemplate).convertAndSend(anyString(), any(Object.class));
        
        // When: 使用不同的 Topic 和 Tag
        rocketMQProducer.sendNormalMessage("ORDER", "CREATE", testPayload);
        rocketMQProducer.sendNormalMessage("PRODUCT", "UPDATE", testPayload);
        
        // Then: 验证目标地址格式正确
        verify(rocketMQTemplate).convertAndSend(eq("ORDER:CREATE"), any(Object.class));
        verify(rocketMQTemplate).convertAndSend(eq("PRODUCT:UPDATE"), any(Object.class));
    }
    
    @Test
    @DisplayName("发送普通消息 - Payload 为 null 时应正常发送")
    void sendNormalMessage_ShouldHandleNullPayload() {
        // Given
        doNothing().when(rocketMQTemplate).convertAndSend(anyString(), nullable(Object.class));
        
        // When: 发送 null payload
        rocketMQProducer.sendNormalMessage(testTopic, testTag, null);
        
        // Then: 验证仍然调用了 send 方法
        verify(rocketMQTemplate, times(1)).convertAndSend(anyString(), nullable(Object.class));
    }
}
