package com.nexusmall.common.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Kafka 连接测试工具类
 * 
 * @author shudl
 * @since 2026-04-02
 */
@Slf4j
public class KafkaConnectionTest {

    private static final String BOOTSTRAP_SERVERS = "10.10.1.40:31000";
    private static final String TEST_TOPIC = "app-logs";
    private static final String TEST_MESSAGE = "Hello Kafka! 这是一条测试消息";

    public static void main(String[] args) {
        log.info("========== Kafka 连接测试开始 ==========");
        log.info("Bootstrap Servers: {}", BOOTSTRAP_SERVERS);
        
        boolean allPassed = true;
        
        // 测试 1：Producer 连接测试
        log.info("\n【测试 1】Producer 连接测试...");
        try {
            allPassed &= testProducer();
        } catch (Exception e) {
            log.error("Producer 测试失败", e);
            allPassed = false;
        }
        
        // 测试 2：Consumer 连接测试
        log.info("\n【测试 2】Consumer 连接测试...");
        try {
            allPassed &= testConsumer();
        } catch (Exception e) {
            log.error("Consumer 测试失败", e);
            allPassed = false;
        }
        
        // 测试 3：AdminClient 连接测试
        log.info("\n【测试 3】AdminClient 连接测试...");
        try {
            allPassed &= testAdminClient();
        } catch (Exception e) {
            log.error("AdminClient 测试失败", e);
            allPassed = false;
        }
        
        log.info("\n========== Kafka 连接测试结束 ==========");
        if (allPassed) {
            log.info("✅ 所有测试通过！Kafka 连接正常！");
        } else {
            log.error("❌ 部分测试失败！请检查 Kafka 配置！");
        }
    }

    /**
     * 测试 Producer 发送消息
     */
    private static boolean testProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.RETRIES_CONFIG, "3");
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000"); // 10 秒超时
        
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            log.info("Producer 创建成功");
            
            log.info("准备发送测试消息到 Topic: {}", TEST_TOPIC);
            ProducerRecord<String, String> record = new ProducerRecord<>(TEST_TOPIC, "test-key", TEST_MESSAGE);
            Future<RecordMetadata> future = producer.send(record);
            
            RecordMetadata metadata = future.get();
            log.info("✅ 消息发送成功！");
            log.info("  Topic: {}", metadata.topic());
            log.info("  Partition: {}", metadata.partition());
            log.info("  Offset: {}", metadata.offset());
            log.info("  Timestamp: {}", metadata.timestamp());
            
            return true;
        } catch (ExecutionException e) {
            log.error("❌ 消息发送失败：{}", e.getCause().getMessage());
            log.error("  可能原因：Topic [{}] 不存在且未开启自动创建", TEST_TOPIC);
            log.error("  错误详情：{}", e.getCause().getClass().getSimpleName());
            return false;
        } catch (Exception e) {
            log.error("❌ 消息发送异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 测试 Consumer 订阅消息
     */
    private static boolean testConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-test-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            log.info("Consumer 创建成功");
            
            // 获取订阅的 topic 列表
            Set<String> topics = consumer.listTopics().keySet();
            log.info("\n📋 Kafka 服务器上的 Topic 列表：");
            if (topics.isEmpty()) {
                log.info("  (空 - 没有任何 topic)");
            } else {
                topics.forEach(topic -> log.info("  - {}", topic));
            }
            
            // 检查 app-logs topic 是否存在
            if (topics.contains(TEST_TOPIC)) {
                log.info("\n✅ Topic [{}] 存在！", TEST_TOPIC);
            } else {
                log.warn("\n⚠️  Topic [{}] 不存在！", TEST_TOPIC);
                log.warn("  这就是为什么日志无法发送到 Kafka 的原因");
                log.warn("  解决方案：");
                log.warn("    1. 在 Kafka 服务器上创建 topic: kafka-topics.sh --create --topic {} --bootstrap-server {} --partitions 3 --replication-factor 1", TEST_TOPIC, BOOTSTRAP_SERVERS);
                log.warn("    2. 或者在 Kafka 配置中启用自动创建：auto.create.topics.enable=true");
            }
            
            return true;
        } catch (Exception e) {
            log.error("❌ Consumer 测试失败：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 测试 AdminClient 管理功能
     */
    private static boolean testAdminClient() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        
        try (AdminClient adminClient = AdminClient.create(props)) {
            log.info("AdminClient 创建成功");
            
            // 列出所有 topic
            Set<String> topics = adminClient.listTopics().names().get();
            log.info("Kafka 中的 Topic 列表：");
            topics.forEach(topic -> log.info("  - {}", topic));
            
            // 尝试获取测试 topic 的详情
            if (topics.contains(TEST_TOPIC)) {
                DescribeTopicsResult describeTopics = adminClient.describeTopics(java.util.Collections.singleton(TEST_TOPIC));
                TopicDescription topicDesc = describeTopics.topicNameValues().get(TEST_TOPIC).get();
                log.info("Topic [{}] 详情:", TEST_TOPIC);
                log.info("  分区数：{}", topicDesc.partitions().size());
                topicDesc.partitions().forEach(partition -> 
                    log.info("    Partition {}: leader={}, replicas={}, isr={}",
                        partition.partition(),
                        partition.leader() != null ? partition.leader().id() : "N/A",
                        partition.replicas(),
                        partition.isr()
                    )
                );
            } else {
                log.warn("测试 Topic [{}] 不存在", TEST_TOPIC);
            }
            
            return true;
        } catch (ExecutionException e) {
            log.error("AdminClient 测试失败：{}", e.getCause().getMessage());
            return false;
        } catch (Exception e) {
            log.error("AdminClient 测试异常：{}", e.getMessage(), e);
            return false;
        }
    }
}
