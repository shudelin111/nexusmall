package com.nexusmall.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 收货地址创建/更新请求
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@Schema(description = "收货地址请求")
public class AddressRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "收货人姓名不能为空")
    @Schema(description = "收货人姓名", required = true)
    private String name;

    @NotBlank(message = "收货人电话不能为空")
    @Schema(description = "收货人电话", required = true)
    private String phoneNumber;

    @Schema(description = "邮政编码")
    private String postCode;

    @NotBlank(message = "省份不能为空")
    @Schema(description = "省份/直辖市", required = true)
    private String province;

    @NotBlank(message = "城市不能为空")
    @Schema(description = "城市", required = true)
    private String city;

    @NotBlank(message = "区不能为空")
    @Schema(description = "区", required = true)
    private String region;

    @NotBlank(message = "详细地址不能为空")
    @Schema(description = "详细地址（街道）", required = true)
    private String detailAddress;

    @Schema(description = "是否为默认地址：0=否，1=是")
    private Integer defaultStatus = 0;
}
