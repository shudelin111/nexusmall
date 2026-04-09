package com.nexusmall.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存流水实体�?
 * <p>
 * 对应数据库表：inventory_stock_log
 * 用于记录所有库存变动操作，便于追踪和审�?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("inventory_stock_log")
public class StockLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 变动数量（正数表示增加，负数表示减少�?
     */
    private Integer changeQuantity;

    /**
     * 变动前库�?
     */
    private Integer beforeStock;

    /**
     * 变动后库�?
     */
    private Integer afterStock;

    /**
     * 操作类型：PURCHASE_IN(采购入库)、ORDER_DEDUCT(订单扣减)、ORDER_CANCEL(订单取消回滚)、ORDER_PAY(订单支付确认)、MANUAL_ADJUST(手动调整)
     */
    private String operationType;

    /**
     * 业务单号（订单号/采购单号等）
     */
    private String businessSn;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
