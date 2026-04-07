package com.nexusmall.cart.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车汇总视图对象 VO
 * <p>
 * 业界标准：
 * - 包含所有购物车项
 * - 包含统计信息（总价、总数量、优惠金额）
 * - 包含失效商品列表
 * - 支持前端直接渲染结算页面
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "购物车汇总")
public class CartSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    /**
     * 有效购物车项列表
     */
    @Schema(description = "有效购物车项列表")
    private List<CartItemVO> validItems;

    /**
     * 失效购物车项列表
     * <p>
     * 商品下架、库存不足等情况
     * </p>
     */
    @Schema(description = "失效购物车项列表")
    private List<CartItemVO> invalidItems;

    // ===============================
    // 统计信息（核心！）
    // ===============================

    /**
     * 有效商品总数
     */
    @Schema(description = "有效商品总数", example = "5")
    private Integer totalQuantity;

    /**
     * 有效商品种类数
     */
    @Schema(description = "有效商品种类数", example = "3")
    private Integer totalItemCount;

    /**
     * 已选中商品数量
     */
    @Schema(description = "已选中商品数量", example = "2")
    private Integer selectedCount;

    /**
     * 原价总额（快照价格 × 数量）
     */
    @Schema(description = "原价总额", example = "23997.00")
    private BigDecimal originalTotalAmount;

    /**
     * 促销总额（促销价格 × 数量）
     */
    @Schema(description = "促销总额", example = "22497.00")
    private BigDecimal promotionTotalAmount;

    /**
     * 优惠总金额
     */
    @Schema(description = "优惠总金额", example = "1500.00")
    private BigDecimal totalDiscountAmount;

    /**
     * 应付总额（已选中商品的实际售价总和）
     */
    @Schema(description = "应付总额", example = "17998.00")
    private BigDecimal payableAmount;

    /**
     * 是否全部选中
     */
    @Schema(description = "是否全部选中", example = "true")
    private Boolean allSelected;

    // ===============================
    // 提示信息
    // ===============================

    /**
     * 提示信息列表
     * <p>
     * 示例: ["2件商品降价了", "1件商品库存不足"]
     * </p>
     */
    @Schema(description = "提示信息列表", example = "[\"2件商品降价了\", \"1件商品库存不足\"]")
    private List<String> tips;

    /**
     * 是否有失效商品
     */
    @Schema(description = "是否有失效商品", example = "false")
    private Boolean hasInvalidItems;

    /**
     * 是否可以结算
     */
    @Schema(description = "是否可以结算", example = "true")
    private Boolean canCheckout;

    /**
     * 不可结算原因
     * <p>
     * 示例: "存在失效商品", "库存不足"
     * </p>
     */
    @Schema(description = "不可结算原因", example = "存在失效商品")
    private String checkoutBlockedReason;
}
