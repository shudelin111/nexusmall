package com.nexusmall.payment.domain.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付渠道配置实体?
 * <p>
 * 对应表：pay_channel_config
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Data
@TableName("pay_channel_config")
public class PayChannelConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 渠道编码：ALIPAY/WECHAT/UNIONPAY
     */
    private String channelCode;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 应用ID（AppID?
     */
    private String appId;

    /**
     * 商户ID（MchID?
     */
    private String mchId;

    /**
     * 私钥（加密存储）
     */
    private String privateKey;

    /**
     * 公钥（加密存储）
     */
    private String publicKey;

    /**
     * API密钥（加密存储）
     */
    private String apiKey;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 网关地址
     */
    private String gatewayUrl;

    /**
     * 是否启用?=禁用?=启用
     */
    private Integer enabled;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    /**
     * 逻辑删除?=未删除，1=已删?
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
