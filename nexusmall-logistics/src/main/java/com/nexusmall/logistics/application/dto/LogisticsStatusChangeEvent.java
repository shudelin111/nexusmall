package com.nexusmall.logistics.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物流状态变更事件DTO
 * <p>
 * 业界标准：
 * - 物流服务发布此事件
 * - 订单服务、通知服务等订阅
 * - 实现微服务间的解耦通信
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsStatusChangeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 物流订单ID
     */
    private Long logisticsOrderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 快递公司名称
     */
    private String expressCompany;

    /**
     * 原物流状态
     */
    private Integer oldStatus;

    /**
     * 新物流状态
     */
    private Integer newStatus;

    /**
     * 物流状态描述
     */
    private String statusDesc;

    /**
     * 轨迹内容（最新一条）
     */
    private String trackContent;

    /**
     * 轨迹地点
     */
    private String trackLocation;

    /**
     * 变更时间
     */
    private LocalDateTime changeTime;

    /**
     * 备注
     */
    private String remark;
}
