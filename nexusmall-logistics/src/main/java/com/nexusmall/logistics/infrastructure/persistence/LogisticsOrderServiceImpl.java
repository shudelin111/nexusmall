package com.nexusmall.logistics.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.logistics.application.dto.LogisticsStatusChangeEvent;
import com.nexusmall.logistics.domain.entity.LogisticsOrder;
import com.nexusmall.logistics.domain.enums.LogisticsStatusEnum;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsOrderMapper;
import com.nexusmall.logistics.application.service.LogisticsEventPublisherService;
import com.nexusmall.logistics.application.service.LogisticsOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流订单服务实现?
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsOrderServiceImpl extends ServiceImpl<LogisticsOrderMapper, LogisticsOrder> implements LogisticsOrderService {

    private final LogisticsEventPublisherService eventPublisherService;

    @Override
    public LogisticsOrder getByOrderSn(String orderSn) {
        return this.baseMapper.selectByOrderSn(orderSn);
    }

    @Override
    public LogisticsOrder getByExpressNo(String expressNo) {
        return this.baseMapper.selectByExpressNo(expressNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogisticsOrder createLogisticsOrder(String orderSn, Long memberId, Long warehouseId,
                                                String expressCompany, String receiverName,
                                                String receiverPhone, String receiverAddress,
                                                BigDecimal freightAmount) {
        log.info("【创建物流订单】orderSn={}, memberId={}, expressCompany={}", orderSn, memberId, expressCompany);

        // 1. 检查是否已存在物流订单
        LogisticsOrder existOrder = this.getByOrderSn(orderSn);
        if (existOrder != null) {
            log.warn("【创建物流订单】订单已存在物流单，logisticsOrderId={}", existOrder.getId());
            return existOrder;
        }

        // 2. 生成快递单号（业界标准：快递公司编?+ 时间?+ 随机数）
        String expressNo = generateExpressNo(expressCompany);

        // 3. 创建物流订单
        LogisticsOrder logisticsOrder = new LogisticsOrder();
        logisticsOrder.setOrderSn(orderSn);
        logisticsOrder.setMemberId(memberId);
        logisticsOrder.setWarehouseId(warehouseId);
        logisticsOrder.setExpressCompany(expressCompany);
        logisticsOrder.setExpressNo(expressNo);
        logisticsOrder.setReceiverName(receiverName);
        logisticsOrder.setReceiverPhone(receiverPhone);
        logisticsOrder.setReceiverAddress(receiverAddress);
        logisticsOrder.setStatus(LogisticsStatusEnum.SHIPPED.getCode()); // 初始状态：已发?
        logisticsOrder.setShipTime(LocalDateTime.now());
        logisticsOrder.setFreightAmount(freightAmount != null ? freightAmount : BigDecimal.ZERO);

        this.save(logisticsOrder);

        log.info("【创建物流订单成功】logisticsOrderId={}, expressNo={}", logisticsOrder.getId(), expressNo);
        return logisticsOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        log.info("【更新物流状态】id={}, status={}", id, status);

        LogisticsOrder order = this.getById(id);
        if (order == null) {
            log.error("【更新物流状态】物流订单不存在，id={}", id);
            return false;
        }

        // 验证状态流转合法?
        if (!isValidStatusTransition(order.getStatus(), status)) {
            log.error("【更新物流状态】非法的状态流转，当前状?{}, 目标状?{}", order.getStatus(), status);
            return false;
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(status);
        boolean success = this.updateById(order);

        if (success) {
            log.info("【更新物流状态成功】id={}, 旧状?{}, 新状?{}", id, oldStatus, status);
            
            // 发布物流状态变更事?
            publishStatusChangeEvent(order, oldStatus, status);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReceive(Long id) {
        log.info("【确认签收】id={}", id);

        LogisticsOrder order = this.getById(id);
        if (order == null) {
            log.error("【确认签收】物流订单不存在，id={}", id);
            return false;
        }

        // 只有运输中的订单才能签收
        if (!LogisticsStatusEnum.IN_TRANSIT.getCode().equals(order.getStatus())) {
            log.error("【确认签收】订单状态不正确，当前状?{}", order.getStatus());
            return false;
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(LogisticsStatusEnum.SIGNED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        boolean success = this.updateById(order);

        if (success) {
            log.info("【确认签收成功】id={}", id);
            
            // 发布物流状态变更事?
            publishStatusChangeEvent(order, oldStatus, LogisticsStatusEnum.SIGNED.getCode());
        }

        return success;
    }

    /**
     * 生成快递单?
     * <p>
     * 业界标准格式：快递公司编?2? + 年月?8? + 序列?6?
     * 例如：SF20260407000001（顺丰）
     * </p>
     *
     * @param expressCompany 快递公司名?
     * @return 快递单?
     */
    private String generateExpressNo(String expressCompany) {
        // 提取快递公司编码（取前2个大写字母）
        String prefix = expressCompany.toUpperCase().replaceAll("[^A-Z]", "");
        if (prefix.length() > 2) {
            prefix = prefix.substring(0, 2);
        } else if (prefix.length() < 2) {
            prefix = prefix + "0";
        }

        // 生成时间戳部?
        String datePart = LocalDateTime.now().toString().substring(0, 10).replace("-", "");

        // 生成随机序列号（6位）
        String sequence = String.format("%06d", (int) (Math.random() * 1000000));

        return prefix + datePart + sequence;
    }

    /**
     * 验证状态流转合法?
     * <p>
     * 合法流转?
     * - 待发?0) -> 已发?1)
     * - 已发?1) -> 运输?2)
     * - 运输?2) -> 已签?3)
     * - 运输?2) -> 异常(4)
     * - 异常(4) -> 运输?2)（异常恢复）
     * </p>
     *
     * @param currentStatus 当前状?
     * @param targetStatus  目标状?
     * @return 是否合法
     */
    private boolean isValidStatusTransition(Integer currentStatus, Integer targetStatus) {
        if (currentStatus.equals(targetStatus)) {
            return false; // 不允许相同状?
        }

        switch (currentStatus) {
            case 0: // 待发?
                return targetStatus == 1; // 只能转为已发?
            case 1: // 已发?
                return targetStatus == 2; // 只能转为运输?
            case 2: // 运输?
                return targetStatus == 3 || targetStatus == 4; // 可转为已签收或异?
            case 3: // 已签?
                return false; // 已签收是终态，不能再流?
            case 4: // 异常
                return targetStatus == 2; // 异常恢复为运输中
            default:
                return false;
        }
    }

    /**
     * 发布物流状态变更事?
     * <p>
     * 业界标准?
     * - 状态变更后立即发布事件
     * - 异步通知其他微服务（订单、通知等）
     * - 即使发布失败也不影响主流程（记录日志?
     * </p>
     */
    private void publishStatusChangeEvent(LogisticsOrder order, Integer oldStatus, Integer newStatus) {
        try {
            LogisticsStatusChangeEvent event = LogisticsStatusChangeEvent.builder()
                    .logisticsOrderId(order.getId())
                    .orderSn(order.getOrderSn())
                    .memberId(order.getMemberId())
                    .expressNo(order.getExpressNo())
                    .expressCompany(order.getExpressCompany())
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .statusDesc(LogisticsStatusEnum.getByCode(newStatus).getDescription())
                    .changeTime(LocalDateTime.now())
                    .build();

            boolean success = eventPublisherService.publishStatusChangeEvent(event);
            if (!success) {
                log.warn("【发布物流事件失败】orderSn={}, 但不影响主流程", order.getOrderSn());
            }
        } catch (Exception e) {
            // 事件发布失败不影响主流程，仅记录日志
            log.error("【发布物流事件异常】orderSn={}", order.getOrderSn(), e);
        }
    }
}
