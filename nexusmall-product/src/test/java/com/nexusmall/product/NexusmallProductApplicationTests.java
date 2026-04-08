package com.nexusmall.product;

import com.nexusmall.product.infrastructure.persistence.dao.BrandMapper;
import com.nexusmall.product.infrastructure.persistence.dao.CategoryMapper;
import com.nexusmall.product.infrastructure.persistence.dao.ProductMapper;
import com.nexusmall.product.infrastructure.messaging.StockDecreasedListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.sentinel.enabled=false",
        "rocketmq.name-server=127.0.0.1:9876",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
class NexusmallProductApplicationTests {

    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @MockBean
    private RedisTemplate<Object, Object> redisTemplate;

    @MockBean
    private ProductMapper productMapper;

    @MockBean
    private BrandMapper brandMapper;

    @MockBean
    private CategoryMapper categoryMapper;

    @MockBean
    private StockDecreasedListener stockDecreasedListener;

    @Test
    void contextLoads() {
    }
}
