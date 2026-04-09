package com.nexusmall.logistics.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 退货申请实?
 * <p>
 * 业界标准：
 * - 完整的退货流程管?
 * - 支持退货物流跟?
 * - 退货凭证图片存?
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@TableName("logistics_return_apply")
@Schema(description = "退货申请")
public class LogisticsReturnApply implements Serializable {

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
     * 退货原因
     */
    @Schema(description = "退货原因")
    private String returnReason;

    /**
     * 退货说明
     */
    @Schema(description = "退货说明")
    private String returnDescription;

    /**
     * 退货凭证图片（JSON数组）
     */
    @Schema(description = "退货凭证图片（JSON数组）")
    private String returnImages;

    /**
     * 状态：0=申请中，1=已同意，2=已拒绝，3=已完成
     */
    @Schema(description = "状态：0=申请中，1=已同意，2=已拒绝，3=已完成")
    private Integer status;

    /**
     * 退货快递公司名称
     */
    @Schema(description = "退货快递公司名称")
    private String expressCompany;

    /**
     * 退货快递单号
     */
    @Schema(description = "退货快递单号")
    private String expressNo;

    /**
     * 申请时间
     */
    @Schema(description = "申请时间")
    private LocalDateTime applyTime;

    /**
     * 处理时间
     */
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    /**
     * 收货时间
     */
    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

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
