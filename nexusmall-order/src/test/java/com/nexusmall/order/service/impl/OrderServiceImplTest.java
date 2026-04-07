package com.nexusmall.order.service.impl;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.exception.OrderException;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.application.service.RocketMQProducer;
import com.nexusmall.order.domain.entity.Order;
import com.nexusmall.order.domain.entity.OrderItem;
import com.nexusmall.order.infrastructure.persistence.OrderServiceImpl;
import com.nexusmall.order.infrastructure.persistence.mapper.OrderItemMapper;
import com.nexusmall.order.infrastructure.persistence.mapper.OrderMapper;
import com.nexusmall.order.interfaces.dto.OrderCreateRequest;
import com.nexusmall.order.interfaces.feign.MemberFeignClient;
import com.nexusmall.order.interfaces.feign.ProductFeignService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private ProductFeignService productFeignService;

    @Mock
    private MemberFeignClient memberFeignClient;

    @Mock
    private RocketMQProducer rocketMQProducer;

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderCreateRequest request;

    @BeforeEach
    void setUp() {
        request = new OrderCreateRequest();
        request.setMemberId(1L);
        request.setProductId(100L);
        request.setSkuName("Keyboard");
        request.setPrice(new BigDecimal("99.99"));
        request.setCount(2);
        request.setTotalAmount(new BigDecimal("199.98"));
        request.setPayAmount(new BigDecimal("189.98"));
        request.setFreightAmount(new BigDecimal("10.00"));
        request.setPromotionAmount(BigDecimal.ZERO);
        request.setPaymentType(1);
        request.setReceiverName("Tom");
        request.setReceiverPhone("13800138000");
        request.setReceiverAddress("Beijing");
        request.setRemark("test");
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        when(productFeignService.decreaseStock(anyLong(), anyInt())).thenReturn(Result.success(true));
        when(memberFeignClient.getMemberInfo(anyLong())).thenReturn(Result.success(new HashMap<String, Object>()));
        when(orderMapper.insert(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1000L);
            return 1;
        });
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
        doNothing().when(rocketMQProducer).sendOrderCancelDelayMessage(eq(1000L), anyInt());

        Order result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1000L);
        assertThat(result.getMemberId()).isEqualTo(1L);
        verify(productFeignService).decreaseStock(100L, 2);
        verify(orderMapper).insert(any(Order.class));
        verify(orderItemMapper).insert(any(OrderItem.class));
        verify(rocketMQProducer).sendOrderCancelDelayMessage(eq(1000L), anyInt());
    }

    @Test
    void shouldThrowWhenStockInsufficient() {
        when(productFeignService.decreaseStock(anyLong(), anyInt()))
                .thenReturn(Result.failure(CommonResultCode.INSUFFICIENT_STOCK));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(CommonResultCode.INSUFFICIENT_STOCK.getMessage());

        verify(orderMapper, never()).insert(any(Order.class));
        verify(orderItemMapper, never()).insert(any(OrderItem.class));
    }

    @Test
    void shouldQueryOrderById() {
        Order order = new Order();
        order.setId(1000L);
        order.setOrderSn("ORD202604040001");
        when(orderMapper.selectById(1000L)).thenReturn(order);

        Order result = orderService.getById(1000L);

        assertThat(result).isNotNull();
        assertThat(result.getOrderSn()).isEqualTo("ORD202604040001");
    }

    @Test
    void shouldPayOrder() {
        when(orderMapper.payOrder(eq(1000L), eq(1), any(LocalDateTime.class))).thenReturn(1);
        assertThat(orderService.payOrder(1000L, 1)).isTrue();
    }

    @Test
    void shouldCancelOrder() {
        when(orderMapper.cancelOrder(1000L)).thenReturn(1);
        assertThat(orderService.cancelOrder(1000L)).isTrue();
    }

    @Test
    void shouldDeleteOrder() {
        when(orderMapper.deleteById(1000L)).thenReturn(1);
        when(orderItemMapper.deleteByOrderId(1000L)).thenReturn(1);

        assertThat(orderService.deleteById(1000L)).isTrue();
        verify(orderItemMapper, times(1)).deleteByOrderId(1000L);
    }

    @Test
    void shouldListOrdersByMemberId() {
        Order order = new Order();
        order.setMemberId(1L);
        when(orderMapper.listByMemberId(1L)).thenReturn(Collections.singletonList(order));

        assertThat(orderService.listByMemberId(1L)).hasSize(1);
    }
}
