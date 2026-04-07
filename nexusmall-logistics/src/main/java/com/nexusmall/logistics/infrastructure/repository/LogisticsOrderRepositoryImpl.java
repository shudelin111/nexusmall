package com.nexusmall.logistics.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.logistics.domain.repository.LogisticsOrderRepository;
import com.nexusmall.logistics.domain.entity.LogisticsOrder;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 物流订单仓储实现（基础设施层）
 * <p>
 * 业界标准：
 * - 实现Domain层定义的Repository接口
 * - 使用MyBatis-Plus Mapper进行数据访问
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Repository
@RequiredArgsConstructor
public class LogisticsOrderRepositoryImpl implements LogisticsOrderRepository {

    private final LogisticsOrderMapper logisticsOrderMapper;

    @Override
    public LogisticsOrder findById(Long id) {
        return logisticsOrderMapper.selectById(id);
    }

    @Override
    public LogisticsOrder findByOrderSn(String orderSn) {
        return logisticsOrderMapper.selectByOrderSn(orderSn);
    }

    @Override
    public LogisticsOrder findByExpressNo(String expressNo) {
        return logisticsOrderMapper.selectByExpressNo(expressNo);
    }

    @Override
    public boolean save(LogisticsOrder order) {
        return logisticsOrderMapper.insert(order) > 0;
    }

    @Override
    public boolean update(LogisticsOrder order) {
        return logisticsOrderMapper.updateById(order) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return logisticsOrderMapper.deleteById(id) > 0;
    }
}
