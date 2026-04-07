package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建物流订单请求VO
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "创建物流订单请求")
public class CreateLogisticsOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSn;

    /**
     * 会员ID
     */
    @NotNull(message = "会员ID不能为空")
    @Schema(description = "会员ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;

    /**
     * 仓库ID（可选，不传则智能分配）
     */
    @Schema(description = "仓库ID（可选）")
    private Long warehouseId;

    /**
     * 快递公司编码
     */
    @Schema(description = "快递公司编码")
    private String expressCompany;

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
    @Schema(description = "收货人姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverName;

    /**
     * 收货人电话
     */
    @NotBlank(message = "收货人电话不能为空")
    @Schema(description = "收货人电话", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverPhone;

    /**
     * 收货地址
     */
    @NotBlank(message = "收货地址不能为空")
    @Schema(description = "收货地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverAddress;

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
     * 运费
     */
    @Schema(description = "运费")
    private BigDecimal freightAmount;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;
}
