package com.nexusmall.product.service;

import com.nexusmall.product.vo.BrandVO;

import java.util.List;

public interface BrandService {

    /**
     * 查询所有品牌
     */
    List<BrandVO> list();

    /**
     * 根据 ID 查询品牌
     */
    BrandVO getById(Long id);

    /**
     * 根据名称查询品牌
     */
    BrandVO getByName(String name);

    /**
     * 新增品牌
     */
    int save(BrandVO brandVO);

    /**
     * 更新品牌
     */
    int updateById(BrandVO brandVO);

    /**
     * 删除品牌
     */
    int deleteById(Long id);

    /**
     * 根据状态查询品牌
     */
    List<BrandVO> listByStatus(Integer status);

    /**
     * 根据首字母查询品牌
     */
    List<BrandVO> listByFirstLetter(String firstLetter);
}