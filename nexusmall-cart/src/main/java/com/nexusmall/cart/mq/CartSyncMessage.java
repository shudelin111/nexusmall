package com.nexusmall.cart.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车同步消息
 * <p>
 * 用于RocketMQ异步同步购物车数据到MySQL
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSyncMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（幂等性校验）
     */
    private String messageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * SPU ID
     */
    private Long spuId;

    /**
     * 商品名称快照
     */
    private String productName;

    /**
     * 商品图片快照
     */
    private String productImage;

    /**
     * 价格快照
     */
    private BigDecimal snapshotPrice;

    /**
     * 属性快照JSON
     */
    private String snapshotAttrs;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 是否选中
     */
    private Integer selected;

    /**
     * 操作类型
     * <p>
     * ADD - 新增/更新
     * DELETE - 删除
     * MERGE - 合并
     * </p>
     */
    private String operationType;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
}
