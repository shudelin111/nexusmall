package com.nexusmall.logistics.application.service;

import com.nexusmall.logistics.domain.entity.LogisticsFreightTemplate;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;

import java.util.List;

/**
 * 物流缓存服务接口
 * <p>
 * 业界标准：
 * - 使用Redis缓存热点数据
 * - 减少数据库查询压力
 * - 提升系统响应速度
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsCacheService {

    /**
     * 获取仓库信息（带缓存）
     *
     * @param id 仓库ID
     * @return 仓库信息
     */
    LogisticsWarehouse getWarehouseWithCache(Long id);

    /**
     * 根据编码获取仓库（带缓存）
     *
     * @param warehouseCode 仓库编码
     * @return 仓库信息
     */
    LogisticsWarehouse getWarehouseByCodeWithCache(String warehouseCode);

    /**
     * 获取所有启用的仓库列表（带缓存）
     *
     * @return 仓库列表
     */
    List<LogisticsWarehouse> listEnabledWarehousesWithCache();

    /**
     * 清除仓库缓存
     *
     * @param id 仓库ID
     */
    void evictWarehouseCache(Long id);

    /**
     * 清除所有仓库缓存
     */
    void evictAllWarehouseCache();

    /**
     * 获取运费模板（带缓存）
     *
     * @param id 模板ID
     * @return 运费模板
     */
    LogisticsFreightTemplate getFreightTemplateWithCache(Long id);

    /**
     * 获取默认运费模板（带缓存）
     *
     * @return 默认运费模板
     */
    LogisticsFreightTemplate getDefaultFreightTemplateWithCache();

    /**
     * 清除运费模板缓存
     *
     * @param id 模板ID
     */
    void evictFreightTemplateCache(Long id);

    /**
     * 清除所有运费模板缓存
     */
    void evictAllFreightTemplateCache();
}
