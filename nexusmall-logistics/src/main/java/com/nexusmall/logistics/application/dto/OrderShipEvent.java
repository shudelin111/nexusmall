package com.nexusmall.logistics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单发货事件DTO
 * <p>
 * 业界标准：
 * - 订单服务发布此事件
 * - 物流服务订阅并创建物流单
 * - 包含完整的订单和收货信息
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShipEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 仓库ID（可选）
     */
    private Long warehouseId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区
     */
    private String district;

    /**
     * 运费
     */
    private BigDecimal freightAmount;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 商品总重量（kg）
     */
    private BigDecimal totalWeight;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 备注
     */
    private String remark;
}
