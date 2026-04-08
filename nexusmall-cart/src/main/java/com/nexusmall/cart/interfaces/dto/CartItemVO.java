package com.nexusmall.cart.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项视图对象 VO
 * <p>
 * 业界标准：
 * - 包含商品快照信息（价格、名称、属性）
 * - 包含实时库存状态
 * - 包含促销信息（优惠价、活动标签）
 * - 支持前端直接渲染，无需二次加工
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车项")
public class CartItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ===============================
    // 基础信息
    // ===============================

    /**
     * 购物车项ID
     */
    @Schema(description = "购物车项ID", example = "1")
    private Long id;

    /**
     * SKU ID
     */
    @Schema(description = "SKU ID", example = "10001")
    private Long skuId;

    /**
     * SPU ID
     */
    @Schema(description = "SPU ID", example = "1001")
    private Long spuId;

    // ===============================
    // 商品信息（来自Product服务）
    // ===============================

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "iPhone 15 Pro Max")
    private String productName;

    /**
     * 商品主图
     */
    @Schema(description = "商品主图URL", example = "https://example.com/iphone.jpg")
    private String productImage;

    /**
     * 商品副标题
     */
    @Schema(description = "商品副标题", example = "A17 Pro芯片 | 钛金属边框")
    private String productSubtitle;

    /**
     * 商品属性JSON
     * <p>
     * 示例: {"color":"深空灰","storage":"256GB"}
     * </p>
     */
    @Schema(description = "商品属性JSON", example = "{\"color\":\"深空灰\",\"storage\":\"256GB\"}")
    private String attrs;

    // ===============================
    // 价格信息（关键！）
    // ===============================

    /**
     * 快照价格（加入时的价格，永不改变）
     */
    @Schema(description = "快照价格", example = "8999.00")
    private BigDecimal snapshotPrice;

    /**
     * 当前实时价格（来自Product服务）
     */
    @Schema(description = "当前实时价格", example = "9999.00")
    private BigDecimal currentPrice;

    /**
     * 促销价格（如果有活动）
     */
    @Schema(description = "促销价格", example = "8499.00")
    private BigDecimal promotionPrice;

    /**
     * 小计金额（数量 × 实际售价）
     */
    @Schema(description = "小计金额", example = "8999.00")
    private BigDecimal subtotal;

    /**
     * 价格变动提示
     * <p>
     * 0-无变化, 1-涨价, 2-降价
     * </p>
     */
    @Schema(description = "价格变动提示: 0-无变化 1-涨价 2-降价", example = "0")
    private Integer priceChangeFlag;

    // ===============================
    // 库存和限购
    // ===============================

    /**
     * 购买数量
     */
    @Schema(description = "购买数量", example = "1")
    private Integer quantity;

    /**
     * 库存状态
     * <p>
     * 0-无货, 1-有货, 2-预售
     * </p>
     */
    @Schema(description = "库存状态: 0-无货 1-有货 2-预售", example = "1")
    private Integer stockStatus;

    /**
     * 剩余库存数量
     */
    @Schema(description = "剩余库存数量", example = "99")
    private Integer stockQuantity;

    /**
     * 单次购买上限（null表示无限制）
     */
    @Schema(description = "单次购买上限", example = "5")
    private Integer maxPurchaseLimit;

    /**
     * 是否超过限购数量
     */
    @Schema(description = "是否超过限购数量", example = "false")
    private Boolean exceedLimit;

    // ===============================
    // 选中状态
    // ===============================

    /**
     * 是否选中
     * <p>
     * 0-否, 1-是
     * </p>
     */
    @Schema(description = "是否选中: 0-否 1-是", example = "1")
    private Integer selected;

    // ===============================
    // 促销信息
    // ===============================

    /**
     * 促销标签
     * <p>
     * 示例: ["限时优惠", "满减"]
     * </p>
     */
    @Schema(description = "促销标签列表", example = "[\"限时优惠\", \"满减\"]")
    private java.util.List<String> promotionTags;

    /**
     * 促销活动ID
     */
    @Schema(description = "促销活动ID", example = "PROMO_2026_001")
    private String promotionId;

    /**
     * 优惠金额
     */
    @Schema(description = "优惠金额", example = "500.00")
    private BigDecimal discountAmount;

    // ===============================
    // 有效性标识
    // ===============================

    /**
     * 是否有效
     * <p>
     * false表示商品已下架或失效
     * </p>
     */
    @Schema(description = "是否有效", example = "true")
    private Boolean valid;

    /**
     * 无效原因
     * <p>
     * 示例: "商品已下架", "库存不足"
     * </p>
     */
    @Schema(description = "无效原因", example = "商品已下架")
    private String invalidReason;

    // ===============================
    // 审计字段
    // ===============================

    /**
     * 加入购物车时间
     */
    @Schema(description = "加入购物车时间", example = "2026-04-06T10:30:00")
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    @Schema(description = "最后更新时间", example = "2026-04-06T15:45:00")
    private LocalDateTime updateTime;
}
