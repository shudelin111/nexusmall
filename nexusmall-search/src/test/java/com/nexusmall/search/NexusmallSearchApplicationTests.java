package com.nexusmall.search;

import com.nexusmall.search.infrastructure.client.ProductClient;
import com.nexusmall.search.infrastructure.messaging.consumer.ProductIndexSyncConsumer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.sentinel.enabled=false",
        "rocketmq.name-server=127.0.0.1:9876",
        "spring.elasticsearch.uris=http://127.0.0.1:9200",
        "management.health.elasticsearch.enabled=false",
        "spring.zipkin.enabled=false",
        "spring.sleuth.enabled=false",
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration"
})
class NexusmallSearchApplicationTests {

    @MockBean
    private RestHighLevelClient restHighLevelClient;

    @MockBean
    private ProductClient productClient;

    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @MockBean
    private RedisTemplate<Object, Object> redisTemplate;

    @MockBean
    private ProductIndexSyncConsumer productIndexSyncConsumer;

    @Test
    void contextLoads() {
    }
}
