package com.nexusmall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.nexusmall.product.dao.BrandMapper;
import com.nexusmall.product.service.BrandService;
import com.nexusmall.product.vo.BrandVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<BrandVO> list() {
        return brandMapper.list().stream()
                .map(b -> BeanUtil.copyProperties(b, BrandVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BrandVO getById(Long id) {
        return BeanUtil.copyProperties(brandMapper.selectById(id), BrandVO.class);
    }

    @Override
    public BrandVO getByName(String name) {
        return BeanUtil.copyProperties(brandMapper.selectByName(name), BrandVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(BrandVO brandVO) {
        return brandMapper.insert(BeanUtil.copyProperties(brandVO, 
                com.nexusmall.product.entity.Brand.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(BrandVO brandVO) {
        return brandMapper.updateById(BeanUtil.copyProperties(brandVO, 
                com.nexusmall.product.entity.Brand.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        return brandMapper.deleteById(id);
    }

    @Override
    public List<BrandVO> listByStatus(Integer status) {
        return brandMapper.listByStatus(status).stream()
                .map(b -> BeanUtil.copyProperties(b, BrandVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<BrandVO> listByFirstLetter(String firstLetter) {
        return brandMapper.listByFirstLetter(firstLetter).stream()
                .map(b -> BeanUtil.copyProperties(b, BrandVO.class))
                .collect(Collectors.toList());
    }
}