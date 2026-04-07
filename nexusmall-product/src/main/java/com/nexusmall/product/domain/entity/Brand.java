package com.nexusmall.product.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Brand {

    private Long id;
    private String name;
    private String logo;
    private String description;
    private String firstLetter;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}