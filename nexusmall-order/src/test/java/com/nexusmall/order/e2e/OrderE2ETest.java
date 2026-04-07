package com.nexusmall.order.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexusmall.common.vo.Result;
import com.nexusmall.order.interfaces.feign.ProductFeignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

/**
 * 订单服务端到端测试（API 级别 - 带 Mock）
 * <p>
 * 测试目的：
 * - 验证完整的订单创建流程（HTTP 接口 → Service → Mapper）
 * - 验证数据持久化（订单写入数据库）
 * - 验证响应格式和状态码
 * 
 * 测试策略：
 * - 使用 TestRestTemplate 发送真实 HTTP 请求
 * - Mock 外部服务（ProductFeignService），避免依赖其他微服务
 * - 使用真实的数据库和中间件（MySQL、Redis、RocketMQ）
 * - 这是"服务级 E2E 测试"，介于单元测试和完整 E2E 之间
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("订单服务 E2E 测试（带 Mock）")
class OrderE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // Mock 外部服务依赖（Product 服务）
    @MockBean
    private ProductFeignService productFeignService;
    
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        // Mock Product 服务的库存扣减接口
        given(productFeignService.decreaseStock(anyLong(), anyInt()))
            .willReturn(Result.success(true));
        
        // 准备请求头
        headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
    }

    @Test
    @DisplayName("健康检查接口 - 应返回服务状态 UP")
    void testHealthCheck_ShouldReturnServiceUp() {
        // When: 调用健康检查接口
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/order/ping", 
            String.class
        );
        
        // Then: 验证响应
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            assertThat(jsonNode.get("code").asText()).isEqualTo("10000");
            assertThat(jsonNode.get("data").get("service").asText()).isEqualTo("nexusmall-order");
            assertThat(jsonNode.get("data").get("status").asText()).isEqualTo("UP");
        } catch (Exception e) {
            throw new RuntimeException("解析响应失败", e);
        }
    }

    @Test
    @DisplayName("创建订单完整流程 - 应成功创建订单并扣减库存")
    void testCreateOrderFlow_ShouldSuccess_WhenValidRequest() {
        // Given: 准备订单创建请求
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("memberId", 1L);
        orderRequest.put("productId", 100L);
        orderRequest.put("skuName", "测试商品-E2E");
        orderRequest.put("price", new BigDecimal("99.99"));
        orderRequest.put("count", 1);
        orderRequest.put("totalAmount", new BigDecimal("99.99"));
        orderRequest.put("payAmount", new BigDecimal("99.99"));
        orderRequest.put("freightAmount", new BigDecimal("0.00"));
        orderRequest.put("promotionAmount", BigDecimal.ZERO);
        orderRequest.put("paymentType", 1);
        orderRequest.put("receiverName", "E2E测试用户");
        orderRequest.put("receiverPhone", "13800138000");
        orderRequest.put("receiverAddress", "北京市朝阳区E2E测试地址");
        orderRequest.put("remark", "E2E 测试订单");
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(orderRequest, headers);
        
        // When: 调用创建订单接口
        ResponseEntity<String> response = restTemplate.exchange(
            "/order/create",
            HttpMethod.POST,
            requestEntity,
            String.class
        );
        
        // Then: 验证订单创建成功
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        
        try {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            // 验证响应码
            assertThat(jsonNode.get("code").asText()).isEqualTo("10000");
            
            // 验证订单数据
            JsonNode orderData = jsonNode.get("data");
            assertThat(orderData).isNotNull();
            assertThat(orderData.get("id").asLong()).isGreaterThan(0);
            assertThat(orderData.get("orderSn").asText()).isNotBlank();
            assertThat(orderData.get("memberId").asLong()).isEqualTo(1L);
            assertThat(orderData.get("status").asInt()).isEqualTo(0); // 待支付
            
            System.out.println("✅ E2E 测试通过！订单创建成功，订单号：" + 
                             orderData.get("orderSn").asText());
            
        } catch (Exception e) {
            throw new RuntimeException("解析响应失败: " + response.getBody(), e);
        }
    }

    @Test
    @DisplayName("查询订单 - 应能查询到刚创建的订单")
    void testQueryOrder_ShouldReturnOrder_WhenOrderExists() {
        // Given: 先创建一个订单
        Map<String, Object> orderRequest = createTestOrderRequest();
        HttpEntity<Map<String, Object>> createRequest = new HttpEntity<>(orderRequest, headers);
        
        ResponseEntity<String> createResponse = restTemplate.exchange(
            "/order/create",
            HttpMethod.POST,
            createRequest,
            String.class
        );
        
        Long orderId = null;
        try {
            JsonNode createJson = objectMapper.readTree(createResponse.getBody());
            orderId = createJson.get("data").get("id").asLong();
        } catch (Exception e) {
            throw new RuntimeException("创建订单失败", e);
        }
        
        // When: 查询刚创建的订单
        ResponseEntity<String> queryResponse = restTemplate.getForEntity(
            "/order/" + orderId,
            String.class
        );
        
        // Then: 验证查询成功
        assertThat(queryResponse.getStatusCode().value()).isEqualTo(200);
        
        try {
            JsonNode queryJson = objectMapper.readTree(queryResponse.getBody());
            assertThat(queryJson.get("code").asText()).isEqualTo("10000");
            assertThat(queryJson.get("data").get("id").asLong()).isEqualTo(orderId);
            
            System.out.println("✅ E2E 测试通过！订单查询成功，订单ID：" + orderId);
        } catch (Exception e) {
            throw new RuntimeException("解析响应失败", e);
        }
    }

    /**
     * 创建测试订单请求数据
     */
    private Map<String, Object> createTestOrderRequest() {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("memberId", 1L);
        orderRequest.put("productId", 101L);
        orderRequest.put("skuName", "E2E测试商品-查询");
        orderRequest.put("price", new BigDecimal("199.99"));
        orderRequest.put("count", 1);
        orderRequest.put("totalAmount", new BigDecimal("199.99"));
        orderRequest.put("payAmount", new BigDecimal("199.99"));
        orderRequest.put("freightAmount", BigDecimal.ZERO);
        orderRequest.put("promotionAmount", BigDecimal.ZERO);
        orderRequest.put("paymentType", 1);
        orderRequest.put("receiverName", "E2E查询测试");
        orderRequest.put("receiverPhone", "13800138001");
        orderRequest.put("receiverAddress", "上海市浦东新区E2E测试地址");
        orderRequest.put("remark", "E2E 查询测试订单");
        return orderRequest;
    }
}
