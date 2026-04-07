package com.nexusmall.logistics.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;

import java.util.List;

/**
 * 仓库服务接口
 * <p>
 * 业界标准：
 * - 支持仓库CRUD
 * - 支持智能仓库分配
 * - 支持仓库启用/禁用
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
public interface LogisticsWarehouseService extends IService<LogisticsWarehouse> {

    /**
     * 查询所有启用的仓库
     *
     * @return 仓库列表
     */
    List<LogisticsWarehouse> listEnabledWarehouses();

    /**
     * 根据仓库编码查询仓库
     *
     * @param warehouseCode 仓库编码
     * @return 仓库
     */
    LogisticsWarehouse getByWarehouseCode(String warehouseCode);

    /**
     * 智能分配仓库（根据收货地址）
     * <p>
     * 业界标准：
     * - 优先匹配同省仓库
     * - 其次匹配同城仓库
     * - 最后选择距离最近的仓库
     * </p>
     *
     * @param province 省份
     * @param city     城市
     * @return 仓库ID
     */
    Long assignWarehouse(String province, String city);
}
