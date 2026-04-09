package com.nexusmall.logistics.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.logistics.domain.entity.LogisticsReturnApply;
import com.nexusmall.logistics.domain.enums.ReturnStatusEnum;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsReturnApplyMapper;
import com.nexusmall.logistics.application.service.LogisticsReturnApplyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 退货申请服务实现类
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
public class LogisticsReturnApplyServiceImpl extends ServiceImpl<LogisticsReturnApplyMapper, LogisticsReturnApply> implements LogisticsReturnApplyService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LogisticsReturnApply submitReturnApply(String orderSn, Long memberId, String returnReason,
                                                   String returnDescription, String returnImages) {
        log.info("【提交退货申请】orderSn={}, memberId={}, reason={}", orderSn, memberId, returnReason);

        // 1. 检查是否已有进行中的退货申�?
        List<LogisticsReturnApply> existingApplies = this.listByOrderSn(orderSn);
        for (LogisticsReturnApply apply : existingApplies) {
            if (apply.getStatus().equals(ReturnStatusEnum.APPLYING.getCode()) 
                    || apply.getStatus().equals(ReturnStatusEnum.APPROVED.getCode())) {
                log.warn("【提交退货申请】订单已有进行中的退货申请，applyId={}", apply.getId());
                throw new RuntimeException("订单已有进行中的退货申�?);
            }
        }

        // 2. 创建退货申�?
        LogisticsReturnApply returnApply = new LogisticsReturnApply();
        returnApply.setOrderSn(orderSn);
        returnApply.setMemberId(memberId);
        returnApply.setReturnReason(returnReason);
        returnApply.setReturnDescription(returnDescription);
        returnApply.setReturnImages(returnImages);
        returnApply.setStatus(ReturnStatusEnum.APPLYING.getCode());
        returnApply.setApplyTime(LocalDateTime.now());

        this.save(returnApply);

        log.info("【提交退货申请成功】applyId={}", returnApply.getId());
        return returnApply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveReturnApply(Long id) {
        log.info("【审核退货申�?同意】id={}", id);

        LogisticsReturnApply apply = this.getById(id);
        if (apply == null) {
            log.error("【审核退货申请】退货申请不存在，id={}", id);
            return false;
        }

        // 只有申请中的才能审核
        if (!ReturnStatusEnum.APPLYING.getCode().equals(apply.getStatus())) {
            log.error("【审核退货申请】申请状态不正确，当前状�?{}", apply.getStatus());
            return false;
        }

        apply.setStatus(ReturnStatusEnum.APPROVED.getCode());
        apply.setHandleTime(LocalDateTime.now());
        boolean success = this.updateById(apply);

        if (success) {
            log.info("【审核退货申�?同意成功】id={}", id);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectReturnApply(Long id, String reason) {
        log.info("【审核退货申�?拒绝】id={}, reason={}", id, reason);

        LogisticsReturnApply apply = this.getById(id);
        if (apply == null) {
            log.error("【审核退货申请】退货申请不存在，id={}", id);
            return false;
        }

        // 只有申请中的才能审核
        if (!ReturnStatusEnum.APPLYING.getCode().equals(apply.getStatus())) {
            log.error("【审核退货申请】申请状态不正确，当前状�?{}", apply.getStatus());
            return false;
        }

        apply.setStatus(ReturnStatusEnum.REJECTED.getCode());
        apply.setHandleTime(LocalDateTime.now());
        apply.setReturnDescription(reason); // 将拒绝原因存入说明字�?
        boolean success = this.updateById(apply);

        if (success) {
            log.info("【审核退货申�?拒绝成功】id={}", id);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean fillReturnLogistics(Long id, String expressCompany, String expressNo) {
        log.info("【填写退货物流】id={}, expressCompany={}, expressNo={}", id, expressCompany, expressNo);

        LogisticsReturnApply apply = this.getById(id);
        if (apply == null) {
            log.error("【填写退货物流】退货申请不存在，id={}", id);
            return false;
        }

        // 只有已同意的才能填写物流
        if (!ReturnStatusEnum.APPROVED.getCode().equals(apply.getStatus())) {
            log.error("【填写退货物流】申请状态不正确，当前状�?{}", apply.getStatus());
            return false;
        }

        apply.setExpressCompany(expressCompany);
        apply.setExpressNo(expressNo);
        boolean success = this.updateById(apply);

        if (success) {
            log.info("【填写退货物流成功】id={}", id);
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReturnReceive(Long id) {
        log.info("【确认收到退货】id={}", id);

        LogisticsReturnApply apply = this.getById(id);
        if (apply == null) {
            log.error("【确认收到退货】退货申请不存在，id={}", id);
            return false;
        }

        // 只有已同意且已填写物流的才能确认收货
        if (!ReturnStatusEnum.APPROVED.getCode().equals(apply.getStatus())) {
            log.error("【确认收到退货】申请状态不正确，当前状�?{}", apply.getStatus());
            return false;
        }

        if (apply.getExpressNo() == null || apply.getExpressNo().isEmpty()) {
            log.error("【确认收到退货】未填写退货物流信�?);
            return false;
        }

        apply.setStatus(ReturnStatusEnum.COMPLETED.getCode());
        apply.setReceiveTime(LocalDateTime.now());
        boolean success = this.updateById(apply);

        if (success) {
            log.info("【确认收到退货成功】id={}", id);
        }

        return success;
    }

    @Override
    public List<LogisticsReturnApply> listByOrderSn(String orderSn) {
        return this.baseMapper.selectByOrderSn(orderSn);
    }

    @Override
    public List<LogisticsReturnApply> listByMemberId(Long memberId) {
        return this.baseMapper.selectByMemberId(memberId);
    }
}
