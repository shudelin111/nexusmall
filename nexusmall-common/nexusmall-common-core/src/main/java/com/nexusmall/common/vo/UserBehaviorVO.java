package com.nexusmall.common.vo;

import com.nexusmall.common.enums.UserBehaviorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为日志 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehaviorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 行为类型
     */
    private String behaviorType;

    /**
     * 业务对象 ID（如商品 ID、订单 ID）
     */
    private Long objectId;

    /**
     * 业务对象类型（如 product_id、order_id）
     */
    private String objectType;

    /**
     * 额外信息（JSON 格式）
     */
    private String extraData;

    /**
     * IP 地址
     */
    private String ipAddress;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 发生时间
     */
    private LocalDateTime occurTime;

}
