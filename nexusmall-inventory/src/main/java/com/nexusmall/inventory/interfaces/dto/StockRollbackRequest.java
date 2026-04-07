package com.nexusmall.inventory.interfaces.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 库存回滚请求DTO
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
public class StockRollbackRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    /**
     * 回滚数量
     */
    @NotNull(message = "回滚数量不能为空")
    private Integer quantity;

    /**
     * 业务单号（订单号）
     */
    @NotNull(message = "业务单号不能为空")
    private String businessSn;

    /**
     * 操作人ID
     */
    private Long operatorId;
}
