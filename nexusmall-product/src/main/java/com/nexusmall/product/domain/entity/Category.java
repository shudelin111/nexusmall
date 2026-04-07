package com.nexusmall.product.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {

    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private String icon;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}