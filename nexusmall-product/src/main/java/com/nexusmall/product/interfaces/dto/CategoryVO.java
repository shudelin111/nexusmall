package com.nexusmall.product.interfaces.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private String icon;
    private Integer status;
}
