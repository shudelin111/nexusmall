package com.nexusmall.logistics.application.service;

import com.nexusmall.logistics.domain.calculator.FreightCalculator;
import com.nexusmall.logistics.domain.repository.LogisticsFreightTemplateRepository;
import com.nexusmall.logistics.domain.repository.LogisticsOrderRepository;
import com.nexusmall.logistics.domain.repository.LogisticsWarehouseRepository;
import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import com.nexusmall.logistics.domain.entity.LogisticsOrder;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;
import com.nexusmall.logistics.interfaces.dto.CreateLogisticsOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物流订单应用服务
 * <p>
 * 业界标准：
 * - 编排领域服务创建物流订单
 * - 智能分配仓库
 * - 事务边界控制
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsOrderApplicationService {

    private final LogisticsOrderRepository orderRepository;
    private final LogisticsWarehouseRepository warehouseRepository;
    private final LogisticsFreightTemplateRepository freightTemplateRepository;
    private final FreightCalculator freightCalculator = new FreightCalculator();

    /**
     * 创建物流订单
     *
     * @param request 创建请求
     * @return 物流订单
     */
    @Transactional(rollbackFor = Exception.class)
    public LogisticsOrder createLogisticsOrder(CreateLogisticsOrderRequest request) {
        log.info("【应用服?创建物流订单】orderSn={}, memberId={}", 
                request.getOrderSn(), request.getMemberId());

        // 1. 检查是否已存在物流订单
        LogisticsOrder existOrder = orderRepository.findByOrderSn(request.getOrderSn());
        if (existOrder != null) {
            log.warn("【创建物流订单】订单已存在物流单，logisticsOrderId={}", existOrder.getId());
            return existOrder;
        }

        // 2. 智能分配仓库
        Long warehouseId = request.getWarehouseId();
        if (warehouseId == null && request.getProvince() != null && request.getCity() != null) {
            warehouseId = assignWarehouse(request.getProvince(), request.getCity());
            log.info("【创建物流订单】智能分配仓库，warehouseId={}", warehouseId);
        }

        // 3. 计算运费（如果未提供?
        if (request.getFreightAmount() == null) {
            BigDecimal freightAmount = calculateFreight(request);
            request.setFreightAmount(freightAmount);
            log.info("【创建物流订单】计算运费，freightAmount={}", freightAmount);
        }

        // 4. 创建物流订单
        LogisticsOrder order = new LogisticsOrder();
        order.setOrderSn(request.getOrderSn());
        order.setMemberId(request.getMemberId());
        order.setWarehouseId(warehouseId);
        order.setExpressCompany(request.getExpressCompany());
        order.setReceiverName(request.getReceiverName());
        order.setReceiverPhone(request.getReceiverPhone());
        order.setReceiverAddress(request.getReceiverAddress());
        order.setFreightAmount(request.getFreightAmount());
        order.setRemark(request.getRemark());

        // TODO: 调用领域服务生成快递单号并设置状?
        // order.generateExpressNo();
        // order.ship();

        boolean success = orderRepository.save(order);
        if (!success) {
            throw new RuntimeException("创建物流订单失败");
        }

        log.info("【创建物流订单成功】logisticsOrderId={}, expressNo={}", 
                order.getId(), order.getExpressNo());
        return order;
    }

    /**
     * 智能分配仓库
     */
    private Long assignWarehouse(String province, String city) {
        // 策略1：优先匹配同省仓?
        List<LogisticsWarehouse> warehouses = warehouseRepository.findAllEnabled();
        LogisticsWarehouse sameProvince = warehouses.stream()
                .filter(w -> w.getProvince().equals(province))
                .findFirst()
                .orElse(null);
        
        if (sameProvince != null) {
            return sameProvince.getId();
        }

        // 策略2：其次匹配同城仓?
        LogisticsWarehouse sameCity = warehouses.stream()
                .filter(w -> w.getCity().equals(city))
                .findFirst()
                .orElse(null);
        
        if (sameCity != null) {
            return sameCity.getId();
        }

        // 策略3：使用第一个启用的仓库
        if (!warehouses.isEmpty()) {
            return warehouses.get(0).getId();
        }

        throw new RuntimeException("没有可用的仓库");
    }

    /**
     * 计算运费
     */
    private java.math.BigDecimal calculateFreight(CreateLogisticsOrderRequest request) {
        // 获取默认运费模板
        LogisticsFreightTemplate template = freightTemplateRepository.findDefault();
        if (template == null) {
            throw new RuntimeException("没有可用的运费模板");
        }

        // TODO: 从订单服务获取商品重量/体积/件数
        // 这里暂时返回默认运费
        return java.math.BigDecimal.TEN;
    }
}
