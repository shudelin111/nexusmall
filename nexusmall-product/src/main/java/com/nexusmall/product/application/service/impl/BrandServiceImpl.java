package com.nexusmall.product.application.service.impl;

import com.nexusmall.product.application.service.BrandService;
import com.nexusmall.product.domain.entity.Brand;
import com.nexusmall.product.infrastructure.persistence.dao.BrandMapper;
import com.nexusmall.product.interfaces.dto.BrandVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 品牌服务实现类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandMapper brandMapper;

    @Override
    public List<BrandVO> list() {
        log.info("查询所有品牌");
        
        List<Brand> brands = brandMapper.list();
        return brands.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public BrandVO getById(Long id) {
        log.info("根据ID查询品牌: id={}", id);
        
        Brand brand = brandMapper.selectById(id);
        return brand != null ? convertToVO(brand) : null;
    }

    @Override
    public BrandVO getByName(String name) {
        log.info("根据名称查询品牌: name={}", name);
        
        Brand brand = brandMapper.selectByName(name);
        return brand != null ? convertToVO(brand) : null;
    }

    @Override
    public int save(BrandVO brandVO) {
        log.info("新增品牌: name={}", brandVO.getName());
        
        Brand brand = convertToEntity(brandVO);
        brand.setCreateTime(LocalDateTime.now());
        brand.setUpdateTime(LocalDateTime.now());
        
        return brandMapper.insert(brand);
    }

    @Override
    public int updateById(BrandVO brandVO) {
        log.info("更新品牌: id={}", brandVO.getId());
        
        Brand brand = convertToEntity(brandVO);
        brand.setUpdateTime(LocalDateTime.now());
        
        return brandMapper.updateById(brand);
    }

    @Override
    public int deleteById(Long id) {
        log.info("删除品牌: id={}", id);
        
        return brandMapper.deleteById(id);
    }

    @Override
    public List<BrandVO> listByStatus(Integer status) {
        log.info("根据状态查询品牌: status={}", status);
        
        List<Brand> brands = brandMapper.listByStatus(status);
        return brands.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<BrandVO> listByFirstLetter(String firstLetter) {
        log.info("根据首字母查询品牌: firstLetter={}", firstLetter);
        
        List<Brand> brands = brandMapper.listByFirstLetter(firstLetter);
        return brands.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * Entity 转 VO
     */
    private BrandVO convertToVO(Brand brand) {
        BrandVO vo = new BrandVO();
        BeanUtils.copyProperties(brand, vo);
        return vo;
    }

    /**
     * VO 转 Entity
     */
    private Brand convertToEntity(BrandVO vo) {
        Brand brand = new Brand();
        BeanUtils.copyProperties(vo, brand);
        return brand;
    }
}
