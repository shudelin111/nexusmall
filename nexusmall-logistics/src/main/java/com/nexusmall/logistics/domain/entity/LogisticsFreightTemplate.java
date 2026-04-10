package com.nexusmall.logistics.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运费模板实体
 * <p>
 * 业界标准：
 * - 支持多种计费方式（按重量/体积/件数量
 * - 首重续重计费模型
 * - 包邮门槛设置
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("logistics_freight_template")
@Schema(description = "运费模板")
public class LogisticsFreightTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 模板名称
     */
    @Schema(description = "模板名称")
    private String templateName;

    /**
     * 计费方式：1=按重量，2=按体积，3=按件数
     */
    @Schema(description = "计费方式：1=按重量，2=按体积，3=按件数")
    private Integer chargeType;

    /**
     * 首重（kg）
     */
    @Schema(description = "首重（kg）")
    private BigDecimal firstWeight;

    /**
     * 首重费用（元）
     */
    @Schema(description = "首重费用（元）")
    private BigDecimal firstFee;

    /**
     * 续重（kg）
     */
    @Schema(description = "续重（kg）")
    private BigDecimal continuedWeight;

    /**
     * 续重费用（元）
     */
    @Schema(description = "续重费用（元）")
    private BigDecimal continuedFee;

    /**
     * 包邮门槛（元）
     */
    @Schema(description = "包邮门槛（元）")
    private BigDecimal freeThreshold;

    /**
     * 是否默认模板：0=否，1=是
     */
    @Schema(description = "是否默认模板：0=否，1=是")
    private Integer isDefault;

    /**
     * 状态：0=禁用 1=启用
     */
    @Schema(description = "状态：0=禁用 1=启用")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
