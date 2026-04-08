package com.nexusmall.logistics.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsWarehouseMapper;
import com.nexusmall.logistics.application.service.LogisticsCacheService;
import com.nexusmall.logistics.application.service.LogisticsWarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 仓库服务实现类
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsWarehouseServiceImpl extends ServiceImpl<LogisticsWarehouseMapper, LogisticsWarehouse> implements LogisticsWarehouseService {

    private final LogisticsCacheService cacheService;

    @Override
    public List<LogisticsWarehouse> listEnabledWarehouses() {
        // 使用缓存查询
        return cacheService.listEnabledWarehousesWithCache();
    }

    @Override
    public LogisticsWarehouse getByWarehouseCode(String warehouseCode) {
        // 使用缓存查询
        return cacheService.getWarehouseByCodeWithCache(warehouseCode);
    }

    @Override
    public Long assignWarehouse(String province, String city) {
        log.info("【智能分配仓库】province={}, city={}", province, city);

        List<LogisticsWarehouse> enabledWarehouses = this.listEnabledWarehouses();
        if (enabledWarehouses.isEmpty()) {
            log.error("【智能分配仓库】没有可用的仓库");
            return null;
        }

        // 策略1：优先匹配同省仓库
        List<LogisticsWarehouse> sameProvinceWarehouses = enabledWarehouses.stream()
                .filter(w -> w.getProvince().equals(province))
                .collect(Collectors.toList());

        if (!sameProvinceWarehouses.isEmpty()) {
            log.info("【智能分配仓库】匹配到同省仓库，count={}", sameProvinceWarehouses.size());
            // 同省仓库中选择第一个
            return sameProvinceWarehouses.get(0).getId();
        }

        // 策略2：其次匹配同城仓库（理论上不会走到这里，因为同城必然同省）
        List<LogisticsWarehouse> sameCityWarehouses = enabledWarehouses.stream()
                .filter(w -> w.getCity().equals(city))
                .collect(Collectors.toList());

        if (!sameCityWarehouses.isEmpty()) {
            log.info("【智能分配仓库】匹配到同城仓库，count={}", sameCityWarehouses.size());
            return sameCityWarehouses.get(0).getId();
        }

        // 策略3：选择距离最近的仓库（简化版：选择第一个启用的仓库）
        log.info("【智能分配仓库】未匹配到同省/同城仓库，使用默认仓库");
        return enabledWarehouses.get(0).getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(LogisticsWarehouse entity) {
        boolean success = super.save(entity);
        if (success) {
            // 清除缓存
            cacheService.evictAllWarehouseCache();
            log.info("【新增仓库】已清除缓存，warehouseId={}", entity.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(LogisticsWarehouse entity) {
        boolean success = super.updateById(entity);
        if (success) {
            // 清除缓存
            cacheService.evictWarehouseCache(entity.getId());
            log.info("【更新仓库】已清除缓存，warehouseId={}", entity.getId());
        }
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        boolean success = super.removeById(id);
        if (success) {
            // 清除缓存
            cacheService.evictWarehouseCache((Long) id);
            log.info("【删除仓库】已清除缓存，warehouseId={}", id);
        }
        return success;
    }
}
