package com.nexusmall.common.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * 日志消息监听器
 * 
 * 💡 用途：
 * 1. 接收各微服务发送的日志消息
 * 2. 可以转发到 Elasticsearch、数据库或其他存储系统
 * 3. 用于集中式日志分析和监控
 * 
 * @author nexusmall
 * @since 2026-03-25
 */
@Slf4j
@Component
public class LogMessageListener {

    /**
     * 监听 Kafka 日志 Topic
     * 
     * @param record Kafka 消息记录（包含原始日志内容）
     * @param acknowledgment 手动确认对象
     */
    @KafkaListener(
        topics = "app-logs",              // 监听的 Topic
        groupId = "log-consumer-group",   // 消费者组 ID
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            String logMessage = record.value();
            
            // 方案 1：直接输出到控制台（简单方案）
            log.info("收到远程日志 - Topic: {}, Partition: {}, Offset: {}", 
                    record.topic(), record.partition(), record.offset());
            log.info("日志内容：{}", logMessage);
            
            // 方案 2：转发到 Elasticsearch（推荐生产环境使用）
            // sendToElasticsearch(logMessage);
            
            // 方案 3：写入数据库
            // saveToDatabase(logMessage);
            
            // 方案 4：发送到其他消息队列或分析系统
            // sendToAnalysisSystem(logMessage);
            
            // 手动提交 offset
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("处理日志消息失败", e);
            // 不提交 offset，让消息重新消费
        }
    }

    /**
     * 可选：发送到 Elasticsearch
     * 需要集成 elasticsearch-rest-high-level-client
     */
    private void sendToElasticsearch(String logMessage) {
        // TODO: 集成 Elasticsearch
        // 将日志写入 ES，便于搜索和分析
        log.debug("日志写入 ES: {}", logMessage);
    }

    /**
     * 可选：保存到数据库
     * 需要创建日志表
     */
    private void saveToDatabase(String logMessage) {
        // TODO: 创建日志表，保存重要日志
        log.debug("日志存入 DB: {}", logMessage);
    }

    /**
     * 可选：发送到其他分析系统
     */
    private void sendToAnalysisSystem(String logMessage) {
        // TODO: 发送到 Grafana Loki、Splunk 等
        log.debug("日志发送到分析系统：{}", logMessage);
    }
}
