package com.nexusmall.order.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.order.application.service.OrderService;
import com.nexusmall.order.domain.entity.Order;
import com.nexusmall.order.infrastructure.messaging.OrderCancelListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.sentinel.enabled=false",
                "rocketmq.name-server=127.0.0.1:9876",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
        }
)
@AutoConfigureMockMvc
class OrderE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @MockBean
    private OrderCancelListener orderCancelListener;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1000L);
        order.setOrderSn("ORD202604040001");
        order.setMemberId(1L);
        order.setStatus(0);
        order.setTotalAmount(new BigDecimal("99.99"));
        order.setPayAmount(new BigDecimal("99.99"));
        order.setCreateTime(LocalDateTime.now());
    }

    @Test
    void shouldPassHealthCheck() throws Exception {
        mockMvc.perform(get("/orders/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data.service").value("nexusmall-order"));
    }

    @Test
    void shouldCreateOrderThroughHttpLayer() throws Exception {
        when(orderService.createOrder(any())).thenReturn(order);

        mockMvc.perform(post("/orders/")
                        .header("X-API-Version", "v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":1,\"productId\":100,\"skuName\":\"Keyboard\",\"price\":99.99,\"count\":1,\"totalAmount\":99.99,\"payAmount\":99.99,\"freightAmount\":0,\"promotionAmount\":0,\"receiverName\":\"Tom\",\"receiverPhone\":\"13800138000\",\"receiverAddress\":\"Beijing\",\"paymentType\":1,\"remark\":\"e2e\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data.id").value(1000))
                .andExpect(jsonPath("$.data.orderSn").value("ORD202604040001"));
    }

    @Test
    void shouldQueryOrderThroughHttpLayer() throws Exception {
        when(orderService.getById(eq(1000L))).thenReturn(order);

        mockMvc.perform(get("/orders/1000").header("X-API-Version", "v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("10000"))
                .andExpect(jsonPath("$.data.id").value(1000));
    }
}
