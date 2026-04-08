package com.nexusmall.product.interfaces.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BrandVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String logo;
    private String description;
    private String firstLetter;
    private Integer sortOrder;
    private Integer status;
}
