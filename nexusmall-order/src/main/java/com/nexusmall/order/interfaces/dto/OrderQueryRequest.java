package com.nexusmall.order.interfaces.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单查询请求参数
 * 
 * @author shudl
 * @since 2026-03-26
 */
@Data
public class OrderQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long memberId;

    /**
     * 订单状态（0-待支付，1-已支付，2-已发货，3-已完成，4-已取消）
     */
    private Integer status;

    /**
     * 下单开始时间
     */
    private LocalDateTime startTime;

    /**
     * 下单结束时间
     */
    private LocalDateTime endTime;

}
