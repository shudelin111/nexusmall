package com.nexusmall.cart.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项实体
 * <p>
 * 业界标准：商品快照机制 - 记录加入时的价格、名称、属性，防止商家改价导致用户体验问题
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("cart_item")
public class CartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 商品SPU ID
     */
    private Long spuId;

    // ===============================
    // 商品快照字段(关键！业界标准)
    // ===============================

    /**
     * 商品名称快照(加入时)
     * <p>
     * 为什么需要快照？
     * - 用户将iPhone加入购物车,价格8999元
     * - 商家第二天涨价到9999元
     * - 如果购物车不存快照,用户看到价格突变 → 体验崩塌!
     * - 甚至可能被怀疑"大数据杀熟" → 法律风险!
     * </p>
     */
    private String productName;

    /**
     * 商品主图快照
     */
    private String productImage;

    /**
     * 加入时价格快照(永不改变)
     */
    private BigDecimal snapshotPrice;

    /**
     * 商品属性快照JSON(color/storage等)
     * <p>
     * 示例: {"color": "深空灰", "storage": "128GB"}
     * </p>
     */
    private String snapshotAttrs;

    /**
     * 商品快照版本号
     */
    private Integer snapshotVersion;

    /**
     * 快照创建时间
     */
    private LocalDateTime snapshotTime;

    // ===============================
    // 购物车业务字段
    // ===============================

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 是否选中: 0-否 1-是
     */
    private Integer selected;

    // ===============================
    // 审计字段
    // ===============================

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
