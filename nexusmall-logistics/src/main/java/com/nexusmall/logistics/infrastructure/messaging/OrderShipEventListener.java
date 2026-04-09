package com.nexusmall.logistics.infrastructure.messaging;

import com.alibaba.fastjson.JSON;
import com.nexusmall.logistics.domain.constants.LogisticsConstants;
import com.nexusmall.logistics.application.dto.OrderShipEvent;
import com.nexusmall.logistics.domain.entity.LogisticsOrder;
import com.nexusmall.logistics.application.service.LogisticsOrderService;
import com.nexusmall.logistics.application.service.LogisticsWarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单发货事件监听器
 * <p>
 * 业界标准：
 * - 监听订单服务的发货事件
 * - 自动创建物流订单
 * - 智能分配仓库
 * - 幂等性保证（重复消息不重复处理）
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = LogisticsConstants.Topic.ORDER_SHIP_EVENT,
        consumerGroup = LogisticsConstants.ConsumerGroup.LOGISTICS_CONSUMER_GROUP,
        selectorExpression = LogisticsConstants.Tag.ORDER_SHIP
)
public class OrderShipEventListener implements RocketMQListener<String> {

    private final LogisticsOrderService logisticsOrderService;
    private final LogisticsWarehouseService warehouseService;

    @Override
    public void onMessage(String message) {
        log.info("【收到订单发货事件】message={}", message);

        try {
            // 1. 解析事件
            OrderShipEvent event = JSON.parseObject(message, OrderShipEvent.class);
            if (event == null || event.getOrderSn() == null) {
                log.error("【订单发货事件】事件数据无效");
                return;
            }

            // 2. 幂等性检查：检查是否已存在物流订单
            LogisticsOrder existOrder = logisticsOrderService.getByOrderSn(event.getOrderSn());
            if (existOrder != null) {
                log.warn("【订单发货事件】物流订单已存在，orderSn={}, logisticsOrderId={}",
                        event.getOrderSn(), existOrder.getId());
                return;
            }

            // 3. 智能分配仓库
            Long warehouseId = event.getWarehouseId();
            if (warehouseId == null && event.getProvince() != null && event.getCity() != null) {
                warehouseId = warehouseService.assignWarehouse(event.getProvince(), event.getCity());
                log.info("【订单发货事件】智能分配仓库，warehouseId={}", warehouseId);
            }

            // 4. 创建物流订单
            LogisticsOrder logisticsOrder = logisticsOrderService.createLogisticsOrder(
                    event.getOrderSn(),
                    event.getMemberId(),
                    warehouseId,
                    LogisticsConstants.DefaultExpress.SF_EXPRESS, // 默认使用顺丰
                    event.getReceiverName(),
                    event.getReceiverPhone(),
                    event.getReceiverAddress(),
                    event.getFreightAmount()
            );

            log.info("【订单发货事件处理成功】orderSn={}, logisticsOrderId={}, expressNo={}",
                    event.getOrderSn(), logisticsOrder.getId(), logisticsOrder.getExpressNo());

        } catch (Exception e) {
            log.error("【订单发货事件处理失败】message={}", message, e);
            // 抛出异常，触发RocketMQ重试机制
            throw new RuntimeException("订单发货事件处理失败", e);
        }
    }
}
