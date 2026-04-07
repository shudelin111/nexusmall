package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流轨迹VO
 * <p>
 * 业界标准：
 * - 用于前端展示物流轨迹
 * - 包含轨迹时间、内容、地点、状态
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "物流轨迹")
public class LogisticsTrackVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 轨迹ID
     */
    @Schema(description = "轨迹ID", example = "1")
    private Long id;

    /**
     * 轨迹时间
     */
    @Schema(description = "轨迹时间", example = "2026-04-07 10:00:00")
    private LocalDateTime trackTime;

    /**
     * 轨迹内容
     */
    @Schema(description = "轨迹内容", example = "快件已到达【上海浦东转运中心】")
    private String trackContent;

    /**
     * 轨迹地点
     */
    @Schema(description = "轨迹地点", example = "上海市浦东新区")
    private String trackLocation;

    /**
     * 轨迹状态：1=已揽件，2=运输中，3=派送中，4=已签收
     */
    @Schema(description = "轨迹状态：1=已揽件，2=运输中，3=派送中，4=已签收", example = "2")
    private Integer trackStatus;

    /**
     * 轨迹状态描述
     */
    @Schema(description = "轨迹状态描述", example = "运输中")
    private String trackStatusDesc;
}
