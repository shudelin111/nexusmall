package com.nexusmall.logistics.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.logistics.domain.repository.LogisticsWarehouseRepository;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsWarehouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 仓库仓储实现（基础设施层）
 *
 * @author shudl
 * @since 2026-04-07
 */
@Repository
@RequiredArgsConstructor
public class LogisticsWarehouseRepositoryImpl implements LogisticsWarehouseRepository {

    private final LogisticsWarehouseMapper logisticsWarehouseMapper;

    @Override
    public LogisticsWarehouse findById(Long id) {
        return logisticsWarehouseMapper.selectById(id);
    }

    @Override
    public LogisticsWarehouse findByWarehouseCode(String warehouseCode) {
        LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsWarehouse::getWarehouseCode, warehouseCode);
        return logisticsWarehouseMapper.selectOne(wrapper);
    }

    @Override
    public List<LogisticsWarehouse> findAllEnabled() {
        LambdaQueryWrapper<LogisticsWarehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogisticsWarehouse::getStatus, 1);
        wrapper.orderByAsc(LogisticsWarehouse::getId);
        return logisticsWarehouseMapper.selectList(wrapper);
    }

    @Override
    public boolean save(LogisticsWarehouse warehouse) {
        return logisticsWarehouseMapper.insert(warehouse) > 0;
    }

    @Override
    public boolean update(LogisticsWarehouse warehouse) {
        return logisticsWarehouseMapper.updateById(warehouse) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return logisticsWarehouseMapper.deleteById(id) > 0;
    }
}
