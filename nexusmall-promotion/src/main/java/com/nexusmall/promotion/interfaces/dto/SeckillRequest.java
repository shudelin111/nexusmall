package com.nexusmall.promotion.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 秒杀下单请求DTO
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "秒杀下单请求")
public class SeckillRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @NotNull(message = "SKU ID不能为空")
    @Min(value = 1, message = "SKU ID必须大于0")
    @Schema(description = "SKU ID", required = true, example = "1001")
    private Long skuId;

    /**
     * 用户ID（从Header获取，此处仅用于文档说明）
     */
    @Schema(description = "用户ID（从X-User-ID Header获取）", hidden = true)
    private Long userId;
}
