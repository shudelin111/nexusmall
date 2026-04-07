package com.nexusmall.order.controller;

import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.order.application.service.OrderService;
import com.nexusmall.order.domain.entity.Order;
import com.nexusmall.order.interfaces.controller.OrderController;
import com.nexusmall.order.interfaces.dto.OrderCreateRequest;
import com.nexusmall.order.interfaces.dto.OrderQueryRequest;
import com.nexusmall.order.interfaces.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerTest {

    private OrderService orderService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        orderService = Mockito.mock(OrderService.class);
        OrderController controller = new OrderController();
        ReflectionTestUtils.setField(controller, "orderService", orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnHealthPayload() throws Exception {
        mockMvc.perform(get("/orders/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data.service").value("nexusmall-order"))
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void shouldReturnOrderById() throws Exception {
        Order order = buildOrder();
        Mockito.when(orderService.getById(1000L)).thenReturn(order);

        mockMvc.perform(get("/orders/1000").header("X-API-Version", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data.id").value(1000))
                .andExpect(jsonPath("$.data.orderSn").value("ORD202604040001"));
    }

    @Test
    void shouldReturnNotFoundWhenOrderMissing() throws Exception {
        Mockito.when(orderService.getById(9999L)).thenReturn(null);

        mockMvc.perform(get("/orders/9999").header("X-API-Version", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(CommonResultCode.NOT_FOUND.getErrorCode()));
    }

    @Test
    void shouldCreateOrder() throws Exception {
        Order order = buildOrder();
        Mockito.when(orderService.createOrder(any(OrderCreateRequest.class))).thenReturn(order);

        mockMvc.perform(post("/orders/")
                        .header("X-API-Version", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":1,\"productId\":100,\"skuName\":\"Keyboard\",\"price\":99.99,\"count\":2,\"totalAmount\":199.98,\"payAmount\":189.98,\"freightAmount\":10.00,\"promotionAmount\":0,\"receiverName\":\"Tom\",\"receiverPhone\":\"13800138000\",\"receiverAddress\":\"Beijing\",\"paymentType\":1,\"remark\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data.id").value(1000));

        ArgumentCaptor<OrderCreateRequest> captor = ArgumentCaptor.forClass(OrderCreateRequest.class);
        Mockito.verify(orderService).createOrder(captor.capture());
        assertEquals(Long.valueOf(1L), captor.getValue().getMemberId());
        assertEquals(Long.valueOf(100L), captor.getValue().getProductId());
    }

    @Test
    void shouldSearchOrders() throws Exception {
        Mockito.when(orderService.listByCondition(any(OrderQueryRequest.class)))
                .thenReturn(Collections.singletonList(buildOrder()));

        mockMvc.perform(get("/orders/search")
                        .header("X-API-Version", "v1")
                        .param("memberId", "1")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data[0].memberId").value(1));
    }

    @Test
    void shouldUpdateOrder() throws Exception {
        Mockito.when(orderService.updateById(any(Order.class))).thenReturn(true);

        mockMvc.perform(put("/orders/1000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"receiverName\":\"Jerry\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldDeleteOrder() throws Exception {
        Mockito.when(orderService.deleteById(1000L)).thenReturn(true);

        mockMvc.perform(delete("/orders/1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldPayOrder() throws Exception {
        Mockito.when(orderService.payOrder(1000L, 1)).thenReturn(true);

        mockMvc.perform(post("/orders/1000/pay").param("paymentType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data").value(true));

        Mockito.verify(orderService).payOrder(1000L, 1);
    }

    private Order buildOrder() {
        Order order = new Order();
        order.setId(1000L);
        order.setOrderSn("ORD202604040001");
        order.setMemberId(1L);
        order.setStatus(0);
        order.setTotalAmount(new BigDecimal("199.98"));
        order.setPayAmount(new BigDecimal("189.98"));
        order.setCreateTime(LocalDateTime.now());
        return order;
    }
}
