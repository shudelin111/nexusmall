package com.nexusmall.product.interfaces.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 商品查询请求参数
 * 
 * @author shudl
 * @since 2026-03-26
 */
@Data
public class ProductQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键词（商品名称）
     */
    private String keyword;

    /**
     * 分类 ID
     */
    private Long categoryId;

    /**
     * 品牌 ID
     */
    private Long brandId;

    /**
     * 上架状态（0-下架，1-上架）
     */
    private Integer status;

    /**
     * 用户 ID（用于记录用户行为）
     */
    private Long userId;

    /**
     * 用户名（用于记录用户行为）
     */
    private String userName;

}
