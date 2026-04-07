package com.nexusmall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.OrderException;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.dao.OrderItemMapper;
import com.nexusmall.order.dao.OrderMapper;
import com.nexusmall.order.domain.entity.Order;
import com.nexusmall.order.domain.entity.OrderItem;
import com.nexusmall.order.infrastructure.persistence.OrderServiceImpl;
import com.nexusmall.order.interfaces.feign.ProductFeignService;
import com.nexusmall.order.application.service.RocketMQProducer;
import com.nexusmall.order.interfaces.dto.OrderCreateRequest;
import io.seata.core.context.RootContext;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OrderServiceImpl 单元测试
 * <p>
 * 测试策略：
 * 1. 使用 @Mock 模拟所有外部依赖(Mapper、Feign、MQ)
 * 2. 使用 @InjectMocks 自动注入被测试对象
 * 3. 不启动 Spring 容器，纯单元测试(速度快、隔离性好)
 * 4. 遵循 Given-When-Then 模式
 * </p>
 *
 * @author shudl
 * @since 2026-04-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务实现类测试")
class OrderServiceImplTest {

    // ==================== Mock 依赖对象 ====================
    
    @Mock
    private OrderMapper orderMapper;
    
    @Mock
    private OrderItemMapper orderItemMapper;
    
    @Mock
    private ProductFeignService productFeignService;
    
    @Mock
    private RocketMQProducer rocketMQProducer;
    
    @Mock
    private RocketMQTemplate rocketMQTemplate;
    
    // ==================== 被测试对象 ====================
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    // ==================== 测试数据准备 ====================
    
    private OrderCreateRequest validRequest;
    private Order mockOrder;
    private OrderItem mockOrderItem;
    
    @BeforeEach
    void setUp() {
        // 准备有效的创建订单请求
        validRequest = new OrderCreateRequest();
        validRequest.setMemberId(1L);
        validRequest.setProductId(100L);
        validRequest.setSkuName("测试商品");
        validRequest.setPrice(new BigDecimal("99.99"));
        validRequest.setCount(2);
        validRequest.setTotalAmount(new BigDecimal("199.98"));
        validRequest.setPayAmount(new BigDecimal("189.98"));
        validRequest.setFreightAmount(new BigDecimal("10.00"));
        validRequest.setPromotionAmount(BigDecimal.ZERO);
        validRequest.setPaymentType(1);
        validRequest.setReceiverName("张三");
        validRequest.setReceiverPhone("13800138000");
        validRequest.setReceiverAddress("北京市朝阳区测试地址");
        validRequest.setRemark("测试订单");
        
        // 准备模拟的订单对象
        mockOrder = new Order();
        mockOrder.setId(1000L);
        mockOrder.setOrderSn("ORD202604040001");
        mockOrder.setMemberId(1L);
        mockOrder.setStatus(0);
        mockOrder.setCreateTime(LocalDateTime.now());
        mockOrder.setUpdateTime(LocalDateTime.now());
        
        // 准备模拟的订单项对象
        mockOrderItem = new OrderItem();
        mockOrderItem.setId(2000L);
        mockOrderItem.setOrderId(1000L);
        mockOrderItem.setOrderSn("ORD202604040001");
    }
    
    // ==================== 测试用例 ====================
    
    @Test
    @DisplayName("创建订单 - 正常场景应成功")
    void createOrder_ShouldSuccess_WhenValidRequest() {
        // Given: 准备测试数据和 Mock 行为
        when(productFeignService.decreaseStock(anyLong(), anyInt()))
            .thenReturn(Result.success(true));
        
        when(orderMapper.insert(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1000L); // 模拟数据库返回自增 ID
                return 1;
            });
        
        when(orderItemMapper.insert(any(OrderItem.class)))
            .thenReturn(1);
        
        doNothing().when(rocketMQProducer).sendOrderCancelDelayMessage(anyLong(), anyInt());
        
        // When: 执行被测试方法
        Order result = orderService.createOrder(validRequest);
        
        // Then: 验证结果和行为
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1000L);
        assertThat(result.getOrderSn()).isNotBlank();
        assertThat(result.getStatus()).isEqualTo(0);
        assertThat(result.getMemberId()).isEqualTo(1L);
        
        // 验证依赖调用次数
        verify(productFeignService, times(1)).decreaseStock(100L, 2);
        verify(orderMapper, times(1)).insert(any(Order.class));
        verify(orderItemMapper, times(1)).insert(any(OrderItem.class));
        verify(rocketMQProducer, times(1)).sendOrderCancelDelayMessage(eq(1000L), anyInt());
    }
    
    @Test
    @DisplayName("创建订单 - 库存不足应抛出异常")
    void createOrder_ShouldThrowException_WhenInsufficientStock() {
        // Given: 模拟库存扣减失败
        when(productFeignService.decreaseStock(anyLong(), anyInt()))
            .thenReturn(Result.failure(CommonResultCode.INSUFFICIENT_STOCK));
        
        // When & Then: 验证抛出 OrderException
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
            .isInstanceOf(OrderException.class)
            .hasMessageContaining(CommonResultCode.INSUFFICIENT_STOCK.getMessage());
        
        // 验证后续操作未被执行
        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }
    
    @Test
    @DisplayName("创建订单 - 订单插入失败应抛出异常")
    void createOrder_ShouldThrowException_WhenOrderInsertFailed() {
        // Given: 模拟库存扣减成功但订单插入失败
        when(productFeignService.decreaseStock(anyLong(), anyInt()))
            .thenReturn(Result.success(true));
        
        when(orderMapper.insert(any(Order.class)))
            .thenReturn(0); // 模拟插入失败
        
        // When & Then: 验证抛出 OrderException
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
            .isInstanceOf(OrderException.class)
            .hasMessageContaining(CommonResultCode.ORDER_CREATE_FAILED.getMessage());
        
        // 验证订单项未被插入
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }
    
    @Test
    @DisplayName("创建订单 - 订单项插入失败应抛出异常")
    void createOrder_ShouldThrowException_WhenOrderItemInsertFailed() {
        // Given: 模拟订单插入成功但订单项插入失败
        when(productFeignService.decreaseStock(anyLong(), anyInt()))
            .thenReturn(Result.success(true));
        
        when(orderMapper.insert(any(Order.class)))
            .thenAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setId(1000L);
                return 1;
            });
        
        when(orderItemMapper.insert(any(OrderItem.class)))
            .thenReturn(0); // 模拟插入失败
        
        // When & Then: 验证抛出 OrderException
        assertThatThrownBy(() -> orderService.createOrder(validRequest))
            .isInstanceOf(OrderException.class)
            .hasMessageContaining(CommonResultCode.ORDER_CREATE_FAILED.getMessage());
    }
    
    @Test
    @DisplayName("根据ID查询订单 - 订单存在应返回订单")
    void getById_ShouldReturnOrder_WhenOrderExists() {
        // Given
        Long orderId = 1000L;
        when(orderMapper.selectById(orderId)).thenReturn(mockOrder);
        
        // When
        Order result = orderService.getById(orderId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(orderId);
        assertThat(result.getOrderSn()).isEqualTo("ORD202604040001");
        
        verify(orderMapper, times(1)).selectById(orderId);
    }
    
    @Test
    @DisplayName("根据ID查询订单 - 订单不存在应返回null")
    void getById_ShouldReturnNull_WhenOrderNotExists() {
        // Given
        Long orderId = 9999L;
        when(orderMapper.selectById(orderId)).thenReturn(null);
        
        // When
        Order result = orderService.getById(orderId);
        
        // Then
        assertThat(result).isNull();
        
        verify(orderMapper, times(1)).selectById(orderId);
    }
    
    @Test
    @DisplayName("支付订单 - 支付成功应返回true")
    void payOrder_ShouldReturnTrue_WhenPaymentSuccess() {
        // Given
        Long orderId = 1000L;
        Integer paymentType = 1;
        when(orderMapper.payOrder(eq(orderId), eq(paymentType), any(LocalDateTime.class)))
            .thenReturn(1);
        
        // When
        boolean result = orderService.payOrder(orderId, paymentType);
        
        // Then
        assertThat(result).isTrue();
        
        verify(orderMapper, times(1)).payOrder(eq(orderId), eq(paymentType), any(LocalDateTime.class));
    }
    
    @Test
    @DisplayName("支付订单 - 支付失败应返回false")
    void payOrder_ShouldReturnFalse_WhenPaymentFailed() {
        // Given
        Long orderId = 1000L;
        Integer paymentType = 1;
        when(orderMapper.payOrder(eq(orderId), eq(paymentType), any(LocalDateTime.class)))
            .thenReturn(0);
        
        // When
        boolean result = orderService.payOrder(orderId, paymentType);
        
        // Then
        assertThat(result).isFalse();
        
        verify(orderMapper, times(1)).payOrder(eq(orderId), eq(paymentType), any(LocalDateTime.class));
    }
    
    @Test
    @DisplayName("取消订单 - 取消成功应返回true")
    void cancelOrder_ShouldReturnTrue_WhenCancelSuccess() {
        // Given
        Long orderId = 1000L;
        when(orderMapper.cancelOrder(orderId)).thenReturn(1);
        
        // When
        boolean result = orderService.cancelOrder(orderId);
        
        // Then
        assertThat(result).isTrue();
        
        verify(orderMapper, times(1)).cancelOrder(orderId);
    }
    
    @Test
    @DisplayName("取消订单 - 取消失败应返回false")
    void cancelOrder_ShouldReturnFalse_WhenCancelFailed() {
        // Given
        Long orderId = 1000L;
        when(orderMapper.cancelOrder(orderId)).thenReturn(0);
        
        // When
        boolean result = orderService.cancelOrder(orderId);
        
        // Then
        assertThat(result).isFalse();
        
        verify(orderMapper, times(1)).cancelOrder(orderId);
    }
}
