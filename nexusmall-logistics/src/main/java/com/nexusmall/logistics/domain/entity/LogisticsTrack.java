package com.nexusmall.logistics.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流轨迹实体
 * <p>
 * 业界标准?
 * - 记录完整物流轨迹链路
 * - 支持第三方物流API同步
 * - 按时间倒序展示
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("logistics_track")
@Schema(description = "物流轨迹")
public class LogisticsTrack implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 物流订单ID
     */
    @Schema(description = "物流订单ID")
    private Long logisticsOrderId;

    /**
     * 快递单?
     */
    @Schema(description = "快递单?)
    private String expressNo;

    /**
     * 轨迹时间
     */
    @Schema(description = "轨迹时间")
    private LocalDateTime trackTime;

    /**
     * 轨迹内容
     */
    @Schema(description = "轨迹内容")
    private String trackContent;

    /**
     * 轨迹地点
     */
    @Schema(description = "轨迹地点")
    private String trackLocation;

    /**
     * 轨迹状态：1=已揽件，2=运输中，3=派送中?=已签?
     */
    @Schema(description = "轨迹状态：1=已揽件，2=运输中，3=派送中?=已签?)
    private Integer trackStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
