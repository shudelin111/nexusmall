package com.nexusmall.logistics.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 填写退货物流请求VO
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Schema(description = "填写退货物流请求")
public class FillReturnLogisticsRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 快递公司编码
     */
    @NotBlank(message = "快递公司编码不能为空")
    @Schema(description = "快递公司编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressCompany;

    /**
     * 快递单号
     */
    @NotBlank(message = "快递单号不能为空")
    @Schema(description = "快递单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressNo;
}
