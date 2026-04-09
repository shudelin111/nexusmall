package com.nexusmall.product.infrastructure.persistence.dao;

import com.nexusmall.product.domain.entity.Brand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrandMapper {

    /**
     * 根据 ID 查询品牌
     */
    Brand selectById(@Param("id") Long id);

    /**
     * 查询所有品?
     */
    List<Brand> list();

    /**
     * 根据名称查询品牌
     */
    Brand selectByName(@Param("name") String name);

    /**
     * 新增品牌
     */
    int insert(Brand brand);

    /**
     * 更新品牌
     */
    int updateById(Brand brand);

    /**
     * 删除品牌
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据状态查询品?
     */
    List<Brand> listByStatus(@Param("status") Integer status);

    /**
     * 根据首字母查询品?
     */
    List<Brand> listByFirstLetter(@Param("firstLetter") String firstLetter);
}
