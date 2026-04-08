package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流订单详情响应VO
 * <p>
 * 业界标准：
 * - 包含物流订单完整信息
 * - 包含物流轨迹列表
 * - 用于前端展示物流详情
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "物流订单详情响应")
public class LogisticsOrderDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物流订单ID
     */
    @Schema(description = "物流订单ID", example = "1")
    private Long id;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号", example = "ORD202604070001")
    private String orderSn;

    /**
     * 会员ID
     */
    @Schema(description = "会员ID", example = "1001")
    private Long memberId;

    /**
     * 仓库ID
     */
    @Schema(description = "仓库ID", example = "1")
    private Long warehouseId;

    /**
     * 仓库名称
     */
    @Schema(description = "仓库名称", example = "华东仓")
    private String warehouseName;

    /**
     * 快递公司名称
     */
    @Schema(description = "快递公司名称", example = "顺丰速运")
    private String expressCompany;

    /**
     * 快递单号
     */
    @Schema(description = "快递单号", example = "SF20260407000001")
    private String expressNo;

    /**
     * 收货人姓名
     */
    @Schema(description = "收货人姓名", example = "张三")
    private String receiverName;

    /**
     * 收货人电话
     */
    @Schema(description = "收货人电话", example = "13800138000")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @Schema(description = "收货地址", example = "上海市浦东新区张江高科技园区XX路XX号")
    private String receiverAddress;

    /**
     * 物流状态：0=待发货，1=已发货，2=运输中，3=已签收，4=异常
     */
    @Schema(description = "物流状态：0=待发货，1=已发货，2=运输中，3=已签收，4=异常", example = "2")
    private Integer status;

    /**
     * 物流状态描述
     */
    @Schema(description = "物流状态描述", example = "运输中")
    private String statusDesc;

    /**
     * 发货时间
     */
    @Schema(description = "发货时间", example = "2026-04-07 10:00:00")
    private LocalDateTime shipTime;

    /**
     * 签收时间
     */
    @Schema(description = "签收时间", example = "2026-04-09 15:30:00")
    private LocalDateTime receiveTime;

    /**
     * 运费
     */
    @Schema(description = "运费", example = "10.00")
    private java.math.BigDecimal freightAmount;

    /**
     * 备注
     */
    @Schema(description = "备注", example = "fragile物品，请轻拿轻放")
    private String remark;

    /**
     * 物流轨迹列表（按时间倒序）
     */
    @Schema(description = "物流轨迹列表")
    private List<LogisticsTrackVO> tracks;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2026-04-07 10:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2026-04-09 15:30:00")
    private LocalDateTime updateTime;
}
