package com.nexusmall.order;

import com.nexusmall.order.infrastructure.messaging.OrderCancelListener;
import com.nexusmall.order.infrastructure.persistence.mapper.OrderItemMapper;
import com.nexusmall.order.infrastructure.persistence.mapper.OrderMapper;
import com.nexusmall.order.interfaces.feign.MemberFeignClient;
import com.nexusmall.order.interfaces.feign.ProductFeignService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.sentinel.enabled=false",
                "rocketmq.name-server=127.0.0.1:9876",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
        }
)
class NexusmallOrderApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Environment environment;

    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private OrderItemMapper orderItemMapper;

    @MockBean
    private ProductFeignService productFeignService;

    @MockBean
    private MemberFeignClient memberFeignClient;

    @MockBean
    private OrderCancelListener orderCancelListener;

    @Test
    void contextLoads() {
        assertNotNull(context);
    }

    @Test
    void shouldExposeApplicationName() {
        assertEquals("nexusmall-order", environment.getProperty("spring.application.name"));
    }

    @Test
    void shouldRegisterKeyBeans() {
        assertNotNull(context.getBean("orderController"));
        assertNotNull(context.getBean("orderServiceImpl"));
        assertNotNull(context.getBean("rocketMQProducer"));
    }
}
