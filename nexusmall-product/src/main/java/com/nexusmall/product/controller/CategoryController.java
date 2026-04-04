package com.nexusmall.product.controller;

import com.nexusmall.common.constant.ResponseMessageConstants;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.product.service.CategoryService;
import com.nexusmall.product.vo.CategoryVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/category")
@Tag(name = "分类管理", description = "商品分类的增删改查")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 查询所有分类
     */
    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.list());
    }

    /**
     * 根据 ID 查询分类
     */
    @GetMapping("/{id}")
    public Result<CategoryVO> getById(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /**
     * 查询一级分类
     */
    @GetMapping("/firstLevel")
    public Result<List<CategoryVO>> listFirstLevel() {
        return Result.success(categoryService.listFirstLevel());
    }

    /**
     * 根据父 ID 查询子分类
     */
    @GetMapping("/listByParentId/{parentId}")
    public Result<List<CategoryVO>> listByParentId(@PathVariable Long parentId) {
        return Result.success(categoryService.listByParentId(parentId));
    }

    /**
     * 新增分类
     */
    @PostMapping("/save")
    public Result<Integer> save(@RequestBody CategoryVO categoryVO) {
        int result = categoryService.save(categoryVO);
        return result > 0 ? Result.success(ResponseMessageConstants.Category.ADD_SUCCESS, result) : Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 更新分类
     */
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody CategoryVO categoryVO) {
        int result = categoryService.updateById(categoryVO);
        return result > 0 ? Result.success(ResponseMessageConstants.Category.UPDATE_SUCCESS, result) : Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        int result = categoryService.deleteById(id);
        return result > 0 ? Result.success(ResponseMessageConstants.Category.DELETE_SUCCESS, result) : Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 根据层级查询分类
     */
    @GetMapping("/listByLevel/{level}")
    public Result<List<CategoryVO>> listByLevel(@PathVariable Integer level) {
        return Result.success(categoryService.listByLevel(level));
    }

    /**
     * 根据状态查询分类
     */
    @GetMapping("/listByStatus/{status}")
    public Result<List<CategoryVO>> listByStatus(@PathVariable Integer status) {
        return Result.success(categoryService.listByStatus(status));
    }
}