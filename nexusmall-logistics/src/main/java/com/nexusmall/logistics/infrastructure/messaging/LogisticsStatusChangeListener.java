package com.nexusmall.logistics.infrastructure.messaging;

import com.alibaba.fastjson.JSON;
import com.nexusmall.logistics.domain.constants.LogisticsConstants;
import com.nexusmall.logistics.application.dto.LogisticsStatusChangeEvent;
import com.nexusmall.logistics.interfaces.feign.NotificationFeignClient;
import com.nexusmall.logistics.interfaces.feign.OrderFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 物流状态变更事件监听器
 * <p>
 * 业界标准：
 * - 监听物流服务的状态变更事件
 * - 同步更新订单服务的物流状态
 * - 发送通知给用户
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = LogisticsConstants.Topic.LOGISTICS_STATUS_CHANGE,
        consumerGroup = "order-logistics-consumer-group",
        selectorExpression = LogisticsConstants.Tag.STATUS_CHANGE
)
public class LogisticsStatusChangeListener implements RocketMQListener<String> {

    private final OrderFeignClient orderFeignClient;
    private final NotificationFeignClient notificationFeignClient;

    @Override
    public void onMessage(String message) {
        log.info("【收到物流状态变更事件】message={}", message);

        try {
            // 1. 解析事件
            LogisticsStatusChangeEvent event = JSON.parseObject(message, LogisticsStatusChangeEvent.class);
            if (event == null || event.getOrderSn() == null) {
                log.error("【物流状态变更事件】事件数据无效");
                return;
            }

            log.info("【处理物流状态变更】orderSn={}, oldStatus={}, newStatus={}",
                    event.getOrderSn(), event.getOldStatus(), event.getNewStatus());

            // 2. 更新订单服务的物流状态
            updateOrderLogisticsStatus(event);

            // 3. 发送通知给用户
            sendNotificationToUser(event);

            log.info("【物流状态变更事件处理成功】orderSn={}", event.getOrderSn());

        } catch (Exception e) {
            log.error("【物流状态变更事件处理失败】message={}", message, e);
            // 抛出异常，触发RocketMQ重试机制
            throw new RuntimeException("物流状态变更事件处理失败", e);
        }
    }

    /**
     * 更新订单服务的物流状态
     */
    private void updateOrderLogisticsStatus(LogisticsStatusChangeEvent event) {
        try {
            // TODO: 根据实际订单服务API调整
            // orderFeignClient.updateLogisticsStatus(event.getLogisticsOrderId(), event.getNewStatus());
            
            log.info("【更新订单物流状态】orderSn={}, status={}", 
                    event.getOrderSn(), event.getNewStatus());
        } catch (Exception e) {
            log.error("【更新订单物流状态失败】orderSn={}", event.getOrderSn(), e);
            // 不抛出异常，避免影响通知发送
        }
    }

    /**
     * 发送通知给用户
     */
    private void sendNotificationToUser(LogisticsStatusChangeEvent event) {
        try {
            // 构建通知内容
            Map<String, Object> notification = new HashMap<>();
            notification.put("memberId", event.getMemberId());
            notification.put("title", "物流状态更新");
            notification.put("content", buildNotificationContent(event));
            notification.put("type", "LOGISTICS_STATUS_CHANGE");
            notification.put("bizId", event.getLogisticsOrderId());

            // TODO: 根据实际通知服务API调整
            // notificationFeignClient.sendLogisticsNotification(notification);
            
            log.info("【发送物流通知】memberId={}, content={}", 
                    event.getMemberId(), notification.get("content"));
        } catch (Exception e) {
            log.error("【发送物流通知失败】memberId={}", event.getMemberId(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 构建通知内容
     */
    private String buildNotificationContent(LogisticsStatusChangeEvent event) {
        StringBuilder content = new StringBuilder();
        
        switch (event.getNewStatus()) {
            case 1: // 已发货
                content.append("您的订单已发货，快递公司：").append(event.getExpressCompany())
                        .append("，快递单号：").append(event.getExpressNo());
                break;
            case 2: // 运输中
                content.append("您的包裹正在运输中");
                if (event.getTrackContent() != null) {
                    content.append("，最新状态：").append(event.getTrackContent());
                }
                break;
            case 3: // 已签收
                content.append("您的包裹已签收，请确认商品是否完好");
                break;
            case 4: // 异常
                content.append("您的包裹出现异常，请联系客服处理");
                break;
            default:
                content.append("您的订单物流状态已更新");
        }
        
        return content.toString();
    }
}
