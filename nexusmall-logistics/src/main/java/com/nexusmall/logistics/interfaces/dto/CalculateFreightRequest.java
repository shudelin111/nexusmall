package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 计算运费请求VO
 * <p>
 * 业界标准：
 * - 支持多种计费方式
 * - 传入商品重量/体积/件数
 * - 返回计算后的运费
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "计算运费请求")
public class CalculateFreightRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运费模板ID（可选，不传则使用默认模板）
     */
    @Schema(description = "运费模板ID（可选，不传则使用默认模板）", example = "1")
    private Long templateId;

    /**
     * 重量（kg）
     */
    @Schema(description = "重量（kg）", example = "2.5")
    private BigDecimal weight;

    /**
     * 体积（m³）
     */
    @Schema(description = "体积（m³）", example = "0.01")
    private BigDecimal volume;

    /**
     * 件数
     */
    @Schema(description = "件数", example = "3")
    private Integer pieceCount;

    /**
     * 订单金额（用于判断是否包邮）
     */
    @NotNull(message = "订单金额不能为空")
    @Schema(description = "订单金额（用于判断是否包邮）", required = true, example = "199.00")
    private BigDecimal orderAmount;
}
