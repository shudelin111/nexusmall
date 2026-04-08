package com.nexusmall.logistics.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.logistics.domain.repository.LogisticsReturnApplyRepository;
import com.nexusmall.logistics.domain.entity.LogisticsReturnApply;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsReturnApplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 退货申请仓储实现（基础设施层）
 *
 * @author shudl
 * @since 2026-04-07
 */
@Repository
@RequiredArgsConstructor
public class LogisticsReturnApplyRepositoryImpl implements LogisticsReturnApplyRepository {

    private final LogisticsReturnApplyMapper logisticsReturnApplyMapper;

    @Override
    public LogisticsReturnApply findById(Long id) {
        return logisticsReturnApplyMapper.selectById(id);
    }

    @Override
    public List<LogisticsReturnApply> findByOrderSn(String orderSn) {
        LambdaQueryWrapper<LogisticsReturnApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsReturnApply::getOrderSn, orderSn);
        return logisticsReturnApplyMapper.selectList(wrapper);
    }

    @Override
    public List<LogisticsReturnApply> findByMemberId(Long memberId) {
        LambdaQueryWrapper<LogisticsReturnApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsReturnApply::getMemberId, memberId);
        wrapper.orderByDesc(LogisticsReturnApply::getCreateTime);
        return logisticsReturnApplyMapper.selectList(wrapper);
    }

    @Override
    public boolean save(LogisticsReturnApply apply) {
        return logisticsReturnApplyMapper.insert(apply) > 0;
    }

    @Override
    public boolean update(LogisticsReturnApply apply) {
        return logisticsReturnApplyMapper.updateById(apply) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return logisticsReturnApplyMapper.deleteById(id) > 0;
    }
}
