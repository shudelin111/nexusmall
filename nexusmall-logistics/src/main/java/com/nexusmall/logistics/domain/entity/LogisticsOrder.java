package com.nexusmall.logistics.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物流订单实体
 * <p>
 * 业界标准?
 * - 关联订单系统，记录完整物流信?
 * - 支持多仓库发?
 * - 跟踪物流状态流?
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("logistics_order")
@Schema(description = "物流订单")
public class LogisticsOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 订单编号
     */
    @Schema(description = "订单编号")
    private String orderSn;

    /**
     * 会员ID
     */
    @Schema(description = "会员ID")
    private Long memberId;

    /**
     * 发货仓库ID
     */
    @Schema(description = "发货仓库ID")
    private Long warehouseId;

    /**
     * 快递公司名称
     */
    @Schema(description = "快递公司名称")
    private String expressCompany;

    /**
     * 快递单号
     */
    @Schema(description = "快递单号")
    private String expressNo;

    /**
     * 收货人姓名
     */
    @Schema(description = "收货人姓名")
    private String receiverName;

    /**
     * 收货人电话
     */
    @Schema(description = "收货人电话")
    private String receiverPhone;

    /**
     * 收货地址
     */
    @Schema(description = "收货地址")
    private String receiverAddress;

    /**
     * 物流状态：0=待发货，1=已发货，2=运输中，3=已签收，4=异常
     */
    @Schema(description = "物流状态：0=待发货，1=已发货，2=运输中，3=已签收，4=异常")
    private Integer status;

    /**
     * 发货时间
     */
    @Schema(description = "发货时间")
    private LocalDateTime shipTime;

    /**
     * 签收时间
     */
    @Schema(description = "签收时间")
    private LocalDateTime receiveTime;

    /**
     * 运费
     */
    @Schema(description = "运费")
    private BigDecimal freightAmount;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

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
