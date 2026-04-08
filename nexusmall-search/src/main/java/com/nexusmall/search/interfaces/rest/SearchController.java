package com.nexusmall.search.interfaces.rest;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.application.query.ProductSearchQuery;
import com.nexusmall.search.application.service.HotSearchService;
import com.nexusmall.search.interfaces.dto.SearchRequestVO;
import com.nexusmall.search.interfaces.dto.SearchResponseVO;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商品搜索控制器
 */
@Tag(name = "商品搜索", description = "提供商品全文搜索功能")
@Validated
@RestController
@RequestMapping("/")  // 根路径：Gateway已处理/service前缀
public class SearchController {

    private final SearchFacade searchFacade;
    private final HotSearchService hotSearchService;

    public SearchController(SearchFacade searchFacade, HotSearchService hotSearchService) {
        this.searchFacade = searchFacade;
        this.hotSearchService = hotSearchService;
    }

    /**
     * 搜索商品（带热门搜索统计）
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    @Operation(
        summary = "搜索商品",
        description = "根据关键词搜索商品，支持分页、排序、过滤"
    )
    @GetMapping(value = "/products", headers = "X-API-Version=v1")
    public Result<SearchResponseVO> searchProducts(@Validated @ModelAttribute SearchRequestVO request) {
        // 记录热门搜索词
        if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
            hotSearchService.recordSearch(request.getKeyword());
        }
        
        ProductSearchQuery query = new ProductSearchQuery();
        BeanUtils.copyProperties(request, query);
        SearchResultDTO result = searchFacade.search(query);
        SearchResponseVO response = new SearchResponseVO();
        BeanUtils.copyProperties(result, response);
        return Result.success(response);
    }

    /**
     * 获取热门搜索词
     *
     * @param limit 返回数量限制（默认10）
     * @return 热门关键词列表
     */
    @Operation(
        summary = "获取热门搜索词",
        description = "返回最近24小时内搜索次数最多的关键词"
    )
    @GetMapping(value = "/hot-keywords", headers = "X-API-Version=v1")
    public Result<List<Map<String, Object>>> getHotKeywords(
            @Parameter(description = "返回数量限制", example = "10")
            @org.springframework.web.bind.annotation.RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        List<Map<String, Object>> hotKeywords = hotSearchService.getHotKeywords(limit);
        return Result.success(hotKeywords);
    }
}
