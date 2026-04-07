package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 仓库VO
 * <p>
 * 用于前端展示仓库信息
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "仓库信息")
public class WarehouseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 仓库ID
     */
    @Schema(description = "仓库ID", example = "1")
    private Long id;

    /**
     * 仓库名称
     */
    @Schema(description = "仓库名称", example = "华东仓")
    private String warehouseName;

    /**
     * 仓库编码
     */
    @Schema(description = "仓库编码", example = "WH_EAST")
    private String warehouseCode;

    /**
     * 省份
     */
    @Schema(description = "省份", example = "上海市")
    private String province;

    /**
     * 城市
     */
    @Schema(description = "城市", example = "上海市")
    private String city;

    /**
     * 区
     */
    @Schema(description = "区", example = "浦东新区")
    private String region;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址", example = "张江高科技园区XX路XX号")
    private String detailAddress;

    /**
     * 联系人
     */
    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    /**
     * 状态：0=禁用，1=启用
     */
    @Schema(description = "状态：0=禁用，1=启用", example = "1")
    private Integer status;

    /**
     * 状态描述
     */
    @Schema(description = "状态描述", example = "启用")
    private String statusDesc;
}
