package com.nexusmall.inventory.interfaces.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.inventory.interfaces.dto.StockDeductRequest;
import com.nexusmall.inventory.interfaces.dto.StockRollbackRequest;
import com.nexusmall.inventory.application.service.SkuStockService;
import com.nexusmall.inventory.interfaces.dto.SkuStockVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理控制器类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/stocks")  // RESTful资源路径：库存集合
@ApiVersion("v1")  // 标记此 ControllerController 支持 v1 版本
@RequiredArgsConstructor
@Tag(name = "库存管理", description = "SKU库存查询、扣减、回滚等操作")
public class InventoryController {

    private final SkuStockService skuStockService;

    /**
     * 查询SKU库存
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @return 库存信息
     */
    @GetMapping(value = "/{skuId}", headers = "X-API-Version=v1")
    @Operation(summary = "查询SKU库存", description = "根据SKU ID和仓库ID查询库存信息")
    public Result<SkuStockVO> getSkuStock(
            @PathVariable Long skuId,
            @RequestParam Long warehouseId) {
        log.info("查询库存: skuId={}, warehouseId={}", skuId, warehouseId);
        SkuStockVO stock = skuStockService.getSkuStock(skuId, warehouseId);
        return Result.success(stock);
    }

    /**
     * 检查库存是否充足
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @param quantity 需求数量
     * @return 是否充足
     */
    @GetMapping(value = "/{skuId}/availability", headers = "X-API-Version=v1")
    @Operation(summary = "检查库存是否充足", description = "判断指定SKU的库存是否满足需求数量")
    public Result<Boolean> checkStock(
            @PathVariable Long skuId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity) {
        log.info("检查库存: skuId={}, warehouseId={}, quantity={}", skuId, warehouseId, quantity);
        boolean sufficient = skuStockService.checkStockSufficient(skuId, warehouseId, quantity);
        return Result.success(sufficient);
    }

    /**
     * 扣减库存（锁定库存，用于下单）
     *
     * @param skuId SKU ID
     * @param request 扣减请求
     * @return 操作结果
     */
    @PatchMapping(value = "/{skuId}/deduction", headers = "X-API-Version=v1")
    @Operation(summary = "扣减库存", description = "下单时锁定库存，使用分布式锁防止超卖")
    public Result<Void> deductStock(
            @PathVariable Long skuId,
            @Validated @RequestBody StockDeductRequest request) {
        log.info("扣减库存: skuId={}, quantity={}, businessSn={}", 
            request.getSkuId(), request.getQuantity(), request.getBusinessSn());
        
        boolean success = skuStockService.deductStock(request);
        if (success) {
            return Result.success();
        } else {
            return Result.failure("500", "库存扣减失败");
        }
    }

    /**
     * 回滚库存（订单取消）
     *
     * @param skuId SKU ID
     * @param request 回滚请求
     * @return 操作结果
     */
    @PatchMapping(value = "/{skuId}/rollback", headers = "X-API-Version=v1")
    @Operation(summary = "回滚库存", description = "订单取消时回滚已锁定的库存")
    public Result<Void> rollbackStock(
            @PathVariable Long skuId,
            @Validated @RequestBody StockRollbackRequest request) {
        log.info("回滚库存: skuId={}, quantity={}, businessSn={}", 
            request.getSkuId(), request.getQuantity(), request.getBusinessSn());
        
        boolean success = skuStockService.rollbackStock(request);
        if (success) {
            return Result.success();
        } else {
            return Result.failure("500", "库存回滚失败");
        }
    }

    /**
     * 确认库存（订单支付成功）
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @param quantity 数量
     * @param businessSn 业务单号
     * @return 操作结果
     */
    @PatchMapping(value = "/{skuId}/confirmation", headers = "X-API-Version=v1")
    @Operation(summary = "确认库存", description = "订单支付成功后，将锁定库存转为已售")
    public Result<Void> confirmStock(
            @PathVariable Long skuId,
            @RequestParam Long warehouseId,
            @RequestParam Integer quantity,
            @RequestParam String businessSn) {
        log.info("确认库存: skuId={}, quantity={}, businessSn={}", skuId, quantity, businessSn);
        
        boolean success = skuStockService.confirmStock(skuId, warehouseId, quantity, businessSn);
        if (success) {
            return Result.success();
        } else {
            return Result.failure("500", "库存确认失败");
        }
    }
}
