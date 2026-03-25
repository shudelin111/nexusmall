package com.nexusmall.behavior.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户行为日志 Elasticsearch 实体类
 * 
 * @author NexusMall
 * @since 2026-03-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "user_behavior_log")
public class UserBehaviorEsLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一 ID（用户 ID_时间戳）
     */
    @Id
    private String id;

    /**
     * 用户 ID
     */
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 用户名
     */
    @Field(type = FieldType.Keyword)
    private String userName;

    /**
     * 行为类型
     */
    @Field(type = FieldType.Keyword)
    private String behaviorType;

    /**
     * 行为描述
     */
    @Field(type = FieldType.Text)
    private String behaviorDesc;

    /**
     * 业务对象 ID
     */
    @Field(type = FieldType.Long)
    private Long objectId;

    /**
     * 业务对象类型
     */
    @Field(type = FieldType.Keyword)
    private String objectType;

    /**
     * 业务对象名称
     */
    @Field(type = FieldType.Keyword)
    private String objectName;

    /**
     * 额外信息（JSON 格式）
     */
    @Field(type = FieldType.Text)
    private String extraData;

    /**
     * IP 地址
     */
    @Field(type = FieldType.Keyword)
    private String ipAddress;

    /**
     * User-Agent
     */
    @Field(type = FieldType.Text)
    private String userAgent;

    /**
     * 行为发生时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime occurTime;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createTime;
}
