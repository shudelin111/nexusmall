package com.nexusmall.common.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 配置类
 * 
 * 💡 用途：
 * 1. 配置 Kafka 生产者和消费者
 * 2. 支持日志消息的发送和接收
 * 3. 异步处理，不阻塞业务线程
 * 
 * @author shudl
 * @since 2026-03-25
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:10.10.1.1:31000}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:log-consumer-group}")
    private String groupId;

    // ===============================
    // 生产者配置
    // ===============================

    /**
     * Kafka 生产者配置
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "1");          // 确认模式
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);           // 重试次数
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);    // 批量大小
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 100);       // 等待时间
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 缓冲区大小
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka 模板（用于发送消息）
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ===============================
    // 消费者配置
    // ===============================

    /**
     * Kafka 消费者配置
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");  // 从最新位置开始消费
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);     // 手动提交 offset
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka 监听器容器工厂
     * 用于支持 @KafkaListener 注解
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);  // 并发消费者数量
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
