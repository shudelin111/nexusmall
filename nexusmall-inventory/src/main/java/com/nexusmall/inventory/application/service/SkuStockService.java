package com.nexusmall.inventory.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.inventory.interfaces.dto.StockDeductRequest;
import com.nexusmall.inventory.interfaces.dto.StockRollbackRequest;
import com.nexusmall.inventory.domain.entity.SkuStock;
import com.nexusmall.inventory.interfaces.dto.SkuStockVO;

/**
 * SKU库存服务接口
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface SkuStockService extends IService<SkuStock> {

    /**
     * 查询SKU库存
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @return 库存信息
     */
    SkuStockVO getSkuStock(Long skuId, Long warehouseId);

    /**
     * 扣减库存（锁定库存，用于下单）
     * <p>
     * 使用 Redisson 分布式锁防止超卖
     * </p>
     *
     * @param request 扣减请求
     * @return 是否成功
     */
    boolean deductStock(StockDeductRequest request);

    /**
     * 回滚库存（订单取消）
     *
     * @param request 回滚请求
     * @return 是否成功
     */
    boolean rollbackStock(StockRollbackRequest request);

    /**
     * 确认库存（订单支付成功，锁定转已售）
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @param quantity 数量
     * @param businessSn 业务单号
     * @return 是否成功
     */
    boolean confirmStock(Long skuId, Long warehouseId, Integer quantity, String businessSn);

    /**
     * 检查库存是否充足
     *
     * @param skuId SKU ID
     * @param warehouseId 仓库ID
     * @param quantity 需求数量
     * @return 是否充足
     */
    boolean checkStockSufficient(Long skuId, Long warehouseId, Integer quantity);
}
