package com.nexusmall.logistics.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仓库实体
 * <p>
 * 业界标准?
 * - 支持多仓库管?
 * - 智能仓库分配策略
 * - 仓库启用/禁用控制
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("logistics_warehouse")
@Schema(description = "仓库")
public class LogisticsWarehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 仓库名称
     */
    @Schema(description = "仓库名称")
    private String warehouseName;

    /**
     * 仓库编码
     */
    @Schema(description = "仓库编码")
    private String warehouseCode;

    /**
     * 省份
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;

    /**
     * ?
     */
    @Schema(description = "?)
    private String region;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String detailAddress;

    /**
     * 联系?
     */
    @Schema(description = "联系?)
    private String contactPerson;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String contactPhone;

    /**
     * 状态：0=禁用?=启用
     */
    @Schema(description = "状态：0=禁用?=启用")
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
