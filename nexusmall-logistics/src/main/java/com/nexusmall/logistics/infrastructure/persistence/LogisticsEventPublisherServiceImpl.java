package com.nexusmall.logistics.infrastructure.persistence;

import com.alibaba.fastjson.JSON;
import com.nexusmall.logistics.domain.constants.LogisticsConstants;
import com.nexusmall.logistics.application.dto.LogisticsStatusChangeEvent;
import com.nexusmall.logistics.domain.enums.LogisticsStatusEnum;
import com.nexusmall.logistics.application.service.LogisticsEventPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 物流事件发布服务实现?
 * <p>
 * 业界标准：
 * - 使用RocketMQ发布事件
 * - 保证消息可靠投?
 * - 支持事务消息（可选）
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsEventPublisherServiceImpl implements LogisticsEventPublisherService {

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public boolean publishStatusChangeEvent(LogisticsStatusChangeEvent event) {
        try {
            // 1. 验证事件数据
            if (event == null || event.getOrderSn() == null) {
                log.error("【发布物流事件】事件数据无效");
                return false;
            }

            // 2. 构建消息
            String destination = LogisticsConstants.Topic.LOGISTICS_STATUS_CHANGE + ":" 
                    + LogisticsConstants.Tag.STATUS_CHANGE;
            
            Message<String> message = MessageBuilder.withPayload(JSON.toJSONString(event))
                    .setHeader("orderSn", event.getOrderSn())
                    .setHeader("expressNo", event.getExpressNo())
                    .setHeader("newStatus", event.getNewStatus())
                    .build();

            // 3. 发送消息（同步发送，保证可靠性）
            org.apache.rocketmq.client.producer.SendResult sendResult = 
                    rocketMQTemplate.syncSend(destination, message);

            // 4. 检查发送结果
            if (sendResult != null && sendResult.getSendStatus() == 
                    org.apache.rocketmq.client.producer.SendStatus.SEND_OK) {
                log.info("【发布物流事件成功】orderSn={}, expressNo={}, oldStatus={}, newStatus={}, msgId={}",
                        event.getOrderSn(),
                        event.getExpressNo(),
                        event.getOldStatus(),
                        event.getNewStatus(),
                        sendResult.getMsgId());
                return true;
            } else {
                log.error("【发布物流事件失败】orderSn={}, sendStatus={}",
                        event.getOrderSn(),
                        sendResult != null ? sendResult.getSendStatus() : "null");
                return false;
            }

        } catch (Exception e) {
            log.error("【发布物流事件异常】event={}", JSON.toJSONString(event), e);
            return false;
        }
    }

    @Override
    public int batchPublishStatusChangeEvents(List<LogisticsStatusChangeEvent> events) {
        if (events == null || events.isEmpty()) {
            log.warn("【批量发布物流事件】事件列表为空");
            return 0;
        }

        int successCount = 0;
        for (LogisticsStatusChangeEvent event : events) {
            if (publishStatusChangeEvent(event)) {
                successCount++;
            }
        }

        log.info("【批量发布物流事件完成】total={}, success={}", events.size(), successCount);
        return successCount;
    }
}
