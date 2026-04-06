package com.nexusmall.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 添加购物车请求 DTO
 * <p>
 * 业界标准：
 * - 支持商品属性选择（颜色、尺寸等）
 * - 支持购买数量校验
 * - 支持幂等性控制（防止重复添加）
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "添加购物车请求")
public class AddCartRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（从JWT Token中解析，前端无需传递）
     */
    @Schema(description = "用户ID", hidden = true)
    private Long userId;

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    @Schema(description = "SKU ID", example = "10001", required = true)
    private Long skuId;

    /**
     * SPU ID
     */
    @NotNull(message = "SPU ID不能为空")
    @Schema(description = "SPU ID", example = "1001", required = true)
    private Long spuId;

    /**
     * 购买数量
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    @Schema(description = "购买数量", example = "1", required = true)
    private Integer quantity;

    /**
     * 商品属性JSON
     * <p>
     * 示例: {"color":"深空灰","storage":"256GB"}
     * </p>
     */
    @Schema(description = "商品属性JSON", example = "{\"color\":\"深空灰\",\"storage\":\"256GB\"}")
    private String attrs;

    /**
     * 幂等性Token（防止重复提交）
     * <p>
     * 前端生成UUID，后端Redis校验
     * </p>
     */
    @Schema(description = "幂等性Token", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String idempotentToken;
}
