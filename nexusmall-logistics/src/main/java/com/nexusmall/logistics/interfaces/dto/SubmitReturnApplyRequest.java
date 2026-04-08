package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 提交退货申请请求VO
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "提交退货申请请求")
public class SubmitReturnApplyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderSn;

    /**
     * 退货原因
     */
    @NotBlank(message = "退货原因不能为空")
    @Schema(description = "退货原因", requiredMode = Schema.RequiredMode.REQUIRED)
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
}
