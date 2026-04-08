package com.nexusmall.logistics.domain.repository;

import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;

import java.util.List;

/**
 * 仓库仓储接口（领域层）
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsWarehouseRepository {

    /**
     * 根据ID查询
     */
    LogisticsWarehouse findById(Long id);

    /**
     * 根据仓库编码查询
     */
    LogisticsWarehouse findByWarehouseCode(String warehouseCode);

    /**
     * 查询所有启用的仓库
     */
    List<LogisticsWarehouse> findAllEnabled();

    /**
     * 保存仓库
     */
    boolean save(LogisticsWarehouse warehouse);

    /**
     * 更新仓库
     */
    boolean update(LogisticsWarehouse warehouse);

    /**
     * 根据ID删除
     */
    boolean deleteById(Long id);
}
