package com.nexusmall.product.infrastructure.persistence.dao;

import com.nexusmall.product.domain.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 根据 ID 查询分类
     */
    Category selectById(@Param("id") Long id);

    /**
     * 查询所有分?
     */
    List<Category> list();

    /**
     * 查询一级分?
     */
    List<Category> listFirstLevel();

    /**
     * 根据?ID 查询子分?
     */
    List<Category> listByParentId(@Param("parentId") Long parentId);

    /**
     * 新增分类
     */
    int insert(Category category);

    /**
     * 更新分类
     */
    int updateById(Category category);

    /**
     * 删除分类
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据层级查询分类
     */
    List<Category> listByLevel(@Param("level") Integer level);

    /**
     * 根据状态查询分?
     */
    List<Category> listByStatus(@Param("status") Integer status);
}
