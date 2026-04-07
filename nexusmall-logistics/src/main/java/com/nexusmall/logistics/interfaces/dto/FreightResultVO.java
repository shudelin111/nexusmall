package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 运费计算结果VO
 * <p>
 * 业界标准：
 * - 返回详细运费信息
 * - 包含计费说明
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "运费计算结果")
public class FreightResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运费金额
     */
    @Schema(description = "运费金额", example = "15.00")
    private BigDecimal freightAmount;

    /**
     * 是否包邮
     */
    @Schema(description = "是否包邮", example = "false")
    private Boolean isFreeShipping;

    /**
     * 计费方式：1=按重量，2=按体积，3=按件数
     */
    @Schema(description = "计费方式：1=按重量，2=按体积，3=按件数", example = "1")
    private Integer chargeType;

    /**
     * 计费说明
     */
    @Schema(description = "计费说明", example = "首重1kg费用10元，续重每1kg费用5元，总重量2.5kg")
    private String description;

    /**
     * 运费模板ID
     */
    @Schema(description = "运费模板ID", example = "1")
    private Long templateId;

    /**
     * 运费模板名称
     */
    @Schema(description = "运费模板名称", example = "标准运费模板")
    private String templateName;
}
