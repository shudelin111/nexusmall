package com.nexusmall.product.application.service.impl;

import com.nexusmall.product.application.service.CategoryService;
import com.nexusmall.product.domain.entity.Category;
import com.nexusmall.product.infrastructure.persistence.dao.CategoryMapper;
import com.nexusmall.product.interfaces.dto.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryVO> list() {
        log.info("查询所有分类");
        
        List<Category> categories = categoryMapper.list();
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public CategoryVO getById(Long id) {
        log.info("根据ID查询分类: id={}", id);
        
        Category category = categoryMapper.selectById(id);
        return category != null ? convertToVO(category) : null;
    }

    @Override
    public List<CategoryVO> listFirstLevel() {
        log.info("查询一级分类");
        
        List<Category> categories = categoryMapper.listFirstLevel();
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listByParentId(Long parentId) {
        log.info("根据父ID查询子分类: parentId={}", parentId);
        
        List<Category> categories = categoryMapper.listByParentId(parentId);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public int save(CategoryVO categoryVO) {
        log.info("新增分类: name={}", categoryVO.getName());
        
        Category category = convertToEntity(categoryVO);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        
        return categoryMapper.insert(category);
    }

    @Override
    public int updateById(CategoryVO categoryVO) {
        log.info("更新分类: id={}", categoryVO.getId());
        
        Category category = convertToEntity(categoryVO);
        category.setUpdateTime(LocalDateTime.now());
        
        return categoryMapper.updateById(category);
    }

    @Override
    public int deleteById(Long id) {
        log.info("删除分类: id={}", id);
        
        return categoryMapper.deleteById(id);
    }

    @Override
    public List<CategoryVO> listByLevel(Integer level) {
        log.info("根据层级查询分类: level={}", level);
        
        List<Category> categories = categoryMapper.listByLevel(level);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CategoryVO> listByStatus(Integer status) {
        log.info("根据状态查询分类: status={}", status);
        
        List<Category> categories = categoryMapper.listByStatus(status);
        return categories.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * Entity 转 VO
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    /**
     * VO 转 Entity
     */
    private Category convertToEntity(CategoryVO vo) {
        Category category = new Category();
        BeanUtils.copyProperties(vo, category);
        return category;
    }
}
