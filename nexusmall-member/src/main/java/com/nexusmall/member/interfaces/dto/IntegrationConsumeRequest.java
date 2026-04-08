package com.nexusmall.member.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 积分兑换请求
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "积分兑换请求")
public class IntegrationConsumeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "积分数量必须大于0")
    @Schema(description = "消耗积分数量", required = true)
    private Integer integration;

    @NotBlank(message = "兑换类型不能为空")
    @Schema(description = "兑换类型：COUPON(优惠券)/PRODUCT(商品)/CASH(现金)", required = true)
    private String consumeType;

    @Schema(description = "兑换对象 ID（如优惠券ID/商品ID）")
    private Long objectId;

    @Schema(description = "兑换对象名称")
    private String objectName;

    @Schema(description = "抵扣金额（如果是现金兑换）")
    private java.math.BigDecimal amount;

    @Schema(description = "备注")
    private String note;
}
