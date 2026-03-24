package com.nexusmall.product.service;

import com.nexusmall.product.dao.ProductStockDTO;
import com.nexusmall.product.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    /**
     * 查询所有分类
     */
    List<CategoryVO> list();

    /**
     * 根据 ID 查询分类
     */
    CategoryVO getById(Long id);

    /**
     * 查询一级分类
     */
    List<CategoryVO> listFirstLevel();

    /**
     * 根据父 ID 查询子分类
     */
    List<CategoryVO> listByParentId(Long parentId);

    /**
     * 新增分类
     */
    int save(CategoryVO categoryVO);

    /**
     * 更新分类
     */
    int updateById(CategoryVO categoryVO);

    /**
     * 删除分类
     */
    int deleteById(Long id);

    /**
     * 根据层级查询分类
     */
    List<CategoryVO> listByLevel(Integer level);

    /**
     * 根据状态查询分类
     */
    List<CategoryVO> listByStatus(Integer status);
}