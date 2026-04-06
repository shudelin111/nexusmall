package com.nexusmall.product.controller;

import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.constant.ResponseMessageConstants;
import com.nexusmall.common.enums.CommonResultCode;
import com.nexusmall.common.vo.Result;
import com.nexusmall.product.service.BrandService;
import com.nexusmall.product.vo.BrandVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品品牌控制器
 */
@RestController
@RequestMapping("/brands")  // Gateway 已通过 /product/** 路由,StripPrefix 后访问 /brands
@ApiVersion("v1")  // 标记此 Controller 支持 v1 版本
@Tag(name = "品牌管理", description = "商品品牌的增删改查")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 查询所有品牌
     */
    @GetMapping(value = "/list", headers = "X-API-Version=v1")
    public Result<List<BrandVO>> list() {
        return Result.success(brandService.list());
    }

    /**
     * 根据 ID 查询品牌
     */
    @GetMapping(value = "/{id}", headers = "X-API-Version=v1")
    public Result<BrandVO> getById(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }

    /**
     * 根据名称查询品牌
     */
    @GetMapping(value = "/getByName/{name}", headers = "X-API-Version=v1")
    public Result<BrandVO> getByName(@PathVariable String name) {
        return Result.success(brandService.getByName(name));
    }

    /**
     * 新增品牌
     */
    @PostMapping(value = "/save", headers = "X-API-Version=v1")
    public Result<Integer> save(@RequestBody BrandVO brandVO) {
        int result = brandService.save(brandVO);
        return result > 0 ? Result.success(ResponseMessageConstants.Brand.ADD_SUCCESS, result) : Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 更新品牌
     */
    @PutMapping(value = "/update", headers = "X-API-Version=v1")
    public Result<Integer> update(@RequestBody BrandVO brandVO) {
        int result = brandService.updateById(brandVO);
        return result > 0 ? Result.success(ResponseMessageConstants.Brand.UPDATE_SUCCESS, result) : Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 删除品牌
     */
    @DeleteMapping(value = "/delete/{id}", headers = "X-API-Version=v1")
    public Result<Integer> delete(@PathVariable Long id) {
        int result = brandService.deleteById(id);
        return result > 0 ? Result.success(ResponseMessageConstants.Brand.DELETE_SUCCESS, result) : Result.failure(CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 根据状态查询品牌
     */
    @GetMapping(value = "/listByStatus/{status}", headers = "X-API-Version=v1")
    public Result<List<BrandVO>> listByStatus(@PathVariable Integer status) {
        return Result.success(brandService.listByStatus(status));
    }

    /**
     * 根据首字母查询品牌
     */
    @GetMapping(value = "/listByFirstLetter/{firstLetter}", headers = "X-API-Version=v1")
    public Result<List<BrandVO>> listByFirstLetter(@PathVariable String firstLetter) {
        return Result.success(brandService.listByFirstLetter(firstLetter));
    }
}