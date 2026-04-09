package com.nexusmall.logistics.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.logistics.domain.entity.LogisticsWarehouse;
import com.nexusmall.logistics.application.service.LogisticsWarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库管理 Controller
 * <p>
 * 业界标准?
 * - 支持仓库CRUD
 * - 支持智能仓库分配
 * - 支持仓库启用/禁用
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
@ApiVersion("v1")
@Tag(name = "仓库管理", description = "仓库信息查询、智能分配")
public class WarehouseController {

    private final LogisticsWarehouseService warehouseService;

    /**
     * 查询所有启用的仓库
     *
     * @return 仓库列表
     */
    @GetMapping(value = "/enabled", headers = "X-API-Version=v1")
    @Operation(summary = "查询启用的仓库", description = "获取所有可用仓库列表")
    public Result<List<LogisticsWarehouse>> listEnabledWarehouses() {
        log.info("【查询启用的仓库】");
        List<LogisticsWarehouse> warehouses = warehouseService.listEnabledWarehouses();
        return Result.success(warehouses);
    }

    /**
     * 根据仓库编码查询仓库
     *
     * @param warehouseCode 仓库编码
     * @return 仓库信息
     */
    @GetMapping(value = "/code/{warehouseCode}", headers = "X-API-Version=v1")
    @Operation(summary = "根据编码查询仓库", description = "通过仓库编码查询详细信息")
    public Result<LogisticsWarehouse> getByWarehouseCode(
            @Parameter(description = "仓库编码", required = true)
            @PathVariable String warehouseCode) {
        log.info("【查询仓库】warehouseCode={}", warehouseCode);
        LogisticsWarehouse warehouse = warehouseService.getByWarehouseCode(warehouseCode);
        if (warehouse == null) {
            return Result.failure("404", "仓库不存在");
        }
        return Result.success(warehouse);
    }

    /**
     * 智能分配仓库
     *
     * @param province 省份
     * @param city     城市
     * @return 仓库ID
     */
    @GetMapping(value = "/assign", headers = "X-API-Version=v1")
    @Operation(summary = "智能分配仓库", description = "根据收货地址自动分配最优仓库")
    public Result<Long> assignWarehouse(
            @Parameter(description = "省份", required = true)
            @RequestParam String province,
            @Parameter(description = "城市", required = true)
            @RequestParam String city) {
        log.info("【智能分配仓库】province={}, city={}", province, city);
        Long warehouseId = warehouseService.assignWarehouse(province, city);
        if (warehouseId == null) {
            return Result.failure("500", "仓库分配失败");
        }
        return Result.success(warehouseId);
    }

    /**
     * 根据ID查询仓库
     *
     * @param id 仓库ID
     * @return 仓库信息
     */
    @GetMapping(value = "/{id}", headers = "X-API-Version=v1")
    @Operation(summary = "根据ID查询仓库", description = "查询指定仓库的详细信息")
    public Result<LogisticsWarehouse> getById(
            @Parameter(description = "仓库ID", required = true)
            @PathVariable Long id) {
        log.info("【查询仓库】id={}", id);
        LogisticsWarehouse warehouse = warehouseService.getById(id);
        if (warehouse == null) {
            return Result.failure("404", "仓库不存在");
        }
        return Result.success(warehouse);
    }
}
