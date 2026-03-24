package com.nexusmall.product.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.nexusmall.product.dao.CategoryMapper;
import com.nexusmall.product.service.CategoryService;
import com.nexusmall.product.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> list() {
        return categoryMapper.list().stream()
                .map(c -> BeanUtil.copyProperties(c, CategoryVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public CategoryVO getById(Long id) {
        return BeanUtil.copyProperties(categoryMapper.selectById(id), CategoryVO.class);
    }

    @Override
    public List<CategoryVO> listFirstLevel() {
        return categoryMapper.listFirstLevel().stream()
                .map(c -> BeanUtil.copyProperties(c, CategoryVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listByParentId(Long parentId) {
        return categoryMapper.listByParentId(parentId).stream()
                .map(c -> BeanUtil.copyProperties(c, CategoryVO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(CategoryVO categoryVO) {
        return categoryMapper.insert(BeanUtil.copyProperties(categoryVO, 
                com.nexusmall.product.entity.Category.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateById(CategoryVO categoryVO) {
        return categoryMapper.updateById(BeanUtil.copyProperties(categoryVO, 
                com.nexusmall.product.entity.Category.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteById(Long id) {
        return categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryVO> listByLevel(Integer level) {
        return categoryMapper.listByLevel(level).stream()
                .map(c -> BeanUtil.copyProperties(c, CategoryVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listByStatus(Integer status) {
        return categoryMapper.listByStatus(status).stream()
                .map(c -> BeanUtil.copyProperties(c, CategoryVO.class))
                .collect(Collectors.toList());
    }
}