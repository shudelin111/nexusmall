package com.nexusmall.behavior.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为日志实体类
 * 
 * @author shudl
 * @since 2026-03-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_behavior_log")
public class UserBehaviorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名（冗余字段，便于查询）
     */
    private String userName;

    /**
     * 行为类型（如：PLACE_ORDER-下单）
     */
    private String behaviorType;

    /**
     * 行为描述
     */
    private String behaviorDesc;

    /**
     * 业务对象 ID（如商品 ID、订单 ID）
     */
    private Long objectId;

    /**
     * 业务对象类型（如：product_id、order_id）
     */
    private String objectType;

    /**
     * 业务对象名称（冗余字段）
     */
    private String objectName;

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
     * 行为发生时间
     */
    private LocalDateTime occurTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
