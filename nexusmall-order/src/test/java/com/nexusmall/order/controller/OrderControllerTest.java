package com.nexusmall.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.entity.Order;
import com.nexusmall.order.service.OrderService;
import com.nexusmall.order.vo.OrderCreateRequest;
import com.nexusmall.order.vo.OrderQueryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrderController 单元测试
 * <p>
 * 测试策略:
 * 1. 使用 @SpringBootTest + @AutoConfigureMockMvc 加载完整 Spring 容器
 * 2. 使用 @MockBean 模拟 OrderService 依赖
 * 3. 使用 MockMvc 模拟 HTTP 请求
 * 4. 验证响应状态码、JSON 结构、业务逻辑
 * </p>
 *
 * @author shudl
 * @since 2026-04-04
 */
@SpringBootTest(classes = com.nexusmall.order.NexusmallOrderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@DisplayName("订单控制器测试")
class OrderControllerTest {

    // ==================== MockMvc 测试工具 ====================
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // ==================== Mock 依赖 ====================
    
    @MockBean
    private OrderService orderService;
    
    // Mock RocketMQ 相关 Bean,避免启动时初始化失败
    @MockBean
    private com.nexusmall.order.service.RocketMQProducer rocketMQProducer;
    
    @MockBean
    private org.apache.rocketmq.spring.core.RocketMQTemplate rocketMQTemplate;
    
    @MockBean
    private com.nexusmall.order.listener.OrderCancelListener orderCancelListener;
    
    // ==================== 测试数据准备 ====================
    
    private Order mockOrder;
    private OrderCreateRequest createRequest;
    
    @BeforeEach
    void setUp() {
        // 准备模拟订单对象
        mockOrder = new Order();
        mockOrder.setId(1000L);
        mockOrder.setOrderSn("ORD202604040001");
        mockOrder.setMemberId(1L);
        mockOrder.setReceiverName("张三");
        mockOrder.setReceiverPhone("13800138000");
        mockOrder.setReceiverAddress("北京市朝阳区测试地址");
        mockOrder.setTotalAmount(new BigDecimal("199.98"));
        mockOrder.setPayAmount(new BigDecimal("189.98"));
        mockOrder.setStatus(0); // 待支付
        mockOrder.setCreateTime(LocalDateTime.now());
        
        // 准备创建订单请求
        createRequest = new OrderCreateRequest();
        createRequest.setMemberId(1L);
        createRequest.setProductId(100L);
        createRequest.setSkuName("测试商品");
        createRequest.setPrice(new BigDecimal("99.99"));
        createRequest.setCount(2);
        createRequest.setTotalAmount(new BigDecimal("199.98"));
        createRequest.setPayAmount(new BigDecimal("189.98"));
        createRequest.setFreightAmount(new BigDecimal("10.00"));
        createRequest.setPromotionAmount(BigDecimal.ZERO);
        createRequest.setPaymentType(1);
        createRequest.setReceiverName("张三");
        createRequest.setReceiverPhone("13800138000");
        createRequest.setReceiverAddress("北京市朝阳区测试地址");
        createRequest.setRemark("测试订单");
    }
    
    // ==================== 健康检查接口测试 ====================
    
    @Test
    @DisplayName("健康检查 - 应返回服务状态 UP")
    void ping_ShouldReturnServiceStatusUp() throws Exception {
        mockMvc.perform(get("/order/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data.service").value("nexusmall-order"))
            .andExpect(jsonPath("$.data.status").value("UP"));
    }
    
    // ==================== 查询订单接口测试 ====================
    
    @Test
    @DisplayName("根据ID查询订单 - 订单存在应返回订单信息")
    void getOrderById_ShouldReturnOrder_WhenOrderExists() throws Exception {
        // Given
        Long orderId = 1000L;
        given(orderService.getById(orderId)).willReturn(mockOrder);
        
        // When & Then
        mockMvc.perform(get("/order/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data.id").value(orderId))
            .andExpect(jsonPath("$.data.orderSn").value("ORD202604040001"))
            .andExpect(jsonPath("$.data.memberId").value(1));
        
        verify(orderService).getById(orderId);
    }
    
    @Test
    @DisplayName("根据ID查询订单 - 订单不存在应返回 404")
    void getOrderById_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
        // Given
        Long orderId = 9999L;
        given(orderService.getById(orderId)).willReturn(null);
        
        // When & Then
        mockMvc.perform(get("/order/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(CommonResultCode.NOT_FOUND.getErrorCode()));
        
        verify(orderService).getById(orderId);
    }
    
    @Test
    @DisplayName("根据订单号查询订单 - 订单存在应返回订单信息")
    void getOrderByOrderSn_ShouldReturnOrder_WhenOrderExists() throws Exception {
        // Given
        String orderSn = "ORD202604040001";
        given(orderService.getByOrderSn(orderSn)).willReturn(mockOrder);
        
        // When & Then
        mockMvc.perform(get("/order/sn/{orderSn}", orderSn))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data.orderSn").value(orderSn));
        
        verify(orderService).getByOrderSn(orderSn);
    }
    
    @Test
    @DisplayName("查询所有订单 - 应返回订单列表")
    void listOrders_ShouldReturnOrderList() throws Exception {
        // Given
        List<Order> orders = Arrays.asList(mockOrder);
        given(orderService.list()).willReturn(orders);
        
        // When & Then
        mockMvc.perform(get("/order/list"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].id").value(1000));
        
        verify(orderService).list();
    }
    
    @Test
    @DisplayName("根据用户ID查询订单 - 应返回该用户的订单列表")
    void listByMemberId_ShouldReturnUserOrders() throws Exception {
        // Given
        Long memberId = 1L;
        List<Order> orders = Arrays.asList(mockOrder);
        given(orderService.listByMemberId(memberId)).willReturn(orders);
        
        // When & Then
        mockMvc.perform(get("/order/member/{memberId}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].memberId").value(memberId));
        
        verify(orderService).listByMemberId(memberId);
    }
    
    @Test
    @DisplayName("根据状态查询订单 - 应返回指定状态的订单列表")
    void listByStatus_ShouldReturnOrdersByStatus() throws Exception {
        // Given
        Integer status = 0;
        List<Order> orders = Arrays.asList(mockOrder);
        given(orderService.listByStatus(status)).willReturn(orders);
        
        // When & Then
        mockMvc.perform(get("/order/status/{status}", status))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data", hasSize(1)))
            .andExpect(jsonPath("$.data[0].status").value(status));
        
        verify(orderService).listByStatus(status);
    }
    
    @Test
    @DisplayName("条件查询订单 - 应返回符合条件的订单列表")
    void searchOrders_ShouldReturnFilteredOrders() throws Exception {
        // Given
        OrderQueryRequest request = new OrderQueryRequest();
        request.setMemberId(1L);
        request.setStatus(0);
        List<Order> orders = Arrays.asList(mockOrder);
        given(orderService.listByCondition(any(OrderQueryRequest.class))).willReturn(orders);
        
        // When & Then
        mockMvc.perform(get("/order/search")
                .param("memberId", "1")
                .param("status", "0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data", hasSize(1)));
        
        verify(orderService).listByCondition(any(OrderQueryRequest.class));
    }
    
    // ==================== 创建订单接口测试 ====================
    
    @Test
    @DisplayName("创建订单 - 有效请求应成功创建订单")
    void createOrder_ShouldCreateOrder_WhenValidRequest() throws Exception {
        // Given
        given(orderService.createOrder(any(OrderCreateRequest.class))).willReturn(mockOrder);
        
        // When & Then
        mockMvc.perform(post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data.id").value(1000))
            .andExpect(jsonPath("$.data.orderSn").value("ORD202604040001"));
        
        verify(orderService).createOrder(any(OrderCreateRequest.class));
    }
    
    @Test
    @DisplayName("创建订单 - 无效请求应返回参数校验错误")
    void createOrder_ShouldReturnValidationError_WhenInvalidRequest() throws Exception {
        // Given: 准备无效请求(memberId 为空)
        OrderCreateRequest invalidRequest = new OrderCreateRequest();
        // 不设置必填字段
        
        // When & Then
        mockMvc.perform(post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest()); // 400 Bad Request
    }
    
    // ==================== 更新订单接口测试 ====================
    
    @Test
    @DisplayName("更新订单 - 更新成功应返回 true")
    void updateOrder_ShouldReturnTrue_WhenUpdateSuccess() throws Exception {
        // Given
        Long orderId = 1000L;
        Order updateOrder = new Order();
        updateOrder.setReceiverName("李四");
        given(orderService.updateById(any(Order.class))).willReturn(true);
        
        // When & Then
        mockMvc.perform(put("/order/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateOrder)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data").value(true));
        
        verify(orderService).updateById(any(Order.class));
    }
    
    @Test
    @DisplayName("更新订单 - 更新失败应返回系统错误")
    void updateOrder_ShouldReturnError_WhenUpdateFailed() throws Exception {
        // Given
        Long orderId = 1000L;
        Order updateOrder = new Order();
        given(orderService.updateById(any(Order.class))).willReturn(false);
        
        // When & Then
        mockMvc.perform(put("/order/{id}", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateOrder)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(CommonResultCode.SYSTEM_ERROR.getErrorCode()));
    }
    
    // ==================== 删除订单接口测试 ====================
    
    @Test
    @DisplayName("删除订单 - 删除成功应返回 true")
    void deleteOrder_ShouldReturnTrue_WhenDeleteSuccess() throws Exception {
        // Given
        Long orderId = 1000L;
        given(orderService.deleteById(orderId)).willReturn(true);
        
        // When & Then
        mockMvc.perform(delete("/order/{id}", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data").value(true));
        
        verify(orderService).deleteById(orderId);
    }
    
    @Test
    @DisplayName("批量删除订单 - 删除成功应返回 true")
    void batchDeleteOrders_ShouldReturnTrue_WhenDeleteSuccess() throws Exception {
        // Given
        List<Long> ids = Arrays.asList(1000L, 1001L);
        given(orderService.batchDelete(ids)).willReturn(true);
        
        // When & Then
        mockMvc.perform(delete("/order/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data").value(true));
        
        verify(orderService).batchDelete(ids);
    }
    
    // ==================== 支付订单接口测试 ====================
    
    @Test
    @DisplayName("支付订单 - 支付成功应返回 true")
    void payOrder_ShouldReturnTrue_WhenPaymentSuccess() throws Exception {
        // Given
        Long orderId = 1000L;
        Integer paymentType = 1;
        given(orderService.payOrder(orderId, paymentType)).willReturn(true);
        
        // When & Then
        mockMvc.perform(post("/order/{id}/pay", orderId)
                .param("paymentType", String.valueOf(paymentType)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data").value(true));
        
        verify(orderService).payOrder(orderId, paymentType);
    }
    
    @Test
    @DisplayName("支付订单 - 支付失败应返回系统错误")
    void payOrder_ShouldReturnError_WhenPaymentFailed() throws Exception {
        // Given
        Long orderId = 1000L;
        Integer paymentType = 1;
        given(orderService.payOrder(orderId, paymentType)).willReturn(false);
        
        // When & Then
        mockMvc.perform(post("/order/{id}/pay", orderId)
                .param("paymentType", String.valueOf(paymentType)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(CommonResultCode.SYSTEM_ERROR.getErrorCode()));
    }
    
    // ==================== 发货接口测试 ====================
    
    @Test
    @DisplayName("发货 - 发货成功应返回 true")
    void deliverOrder_ShouldReturnTrue_WhenDeliverSuccess() throws Exception {
        // Given
        Long orderId = 1000L;
        given(orderService.deliveryOrder(orderId)).willReturn(true);
        
        // When & Then
        mockMvc.perform(post("/order/{id}/deliver", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data").value(true));
        
        verify(orderService).deliveryOrder(orderId);
    }
    
    // ==================== 确认收货接口测试 ====================
    
    @Test
    @DisplayName("确认收货 - 收货成功应返回 true")
    void receiveOrder_ShouldReturnTrue_WhenReceiveSuccess() throws Exception {
        // Given
        Long orderId = 1000L;
        given(orderService.receiveOrder(orderId)).willReturn(true);
        
        // When & Then
        mockMvc.perform(post("/order/{id}/receive", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data").value(true));
        
        verify(orderService).receiveOrder(orderId);
    }
    
    // ==================== 取消订单接口测试 ====================
    
    @Test
    @DisplayName("取消订单 - 取消成功应返回 true")
    void cancelOrder_ShouldReturnTrue_WhenCancelSuccess() throws Exception {
        // Given
        Long orderId = 1000L;
        given(orderService.cancelOrder(orderId)).willReturn(true);
        
        // When & Then
        mockMvc.perform(post("/order/{id}/cancel", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("10000"))
            .andExpect(jsonPath("$.data").value(true));
        
        verify(orderService).cancelOrder(orderId);
    }
    
    @Test
    @DisplayName("取消订单 - 取消失败应返回系统错误")
    void cancelOrder_ShouldReturnError_WhenCancelFailed() throws Exception {
        // Given
        Long orderId = 1000L;
        given(orderService.cancelOrder(orderId)).willReturn(false);
        
        // When & Then
        mockMvc.perform(post("/order/{id}/cancel", orderId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(CommonResultCode.SYSTEM_ERROR.getErrorCode()));
    }
}
