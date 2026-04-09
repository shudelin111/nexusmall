package com.nexusmall.search.interfaces.rest;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.application.query.SuggestQuery;
import com.nexusmall.search.interfaces.dto.SuggestResponseVO;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 搜索建议控制器（关键字联想/自动补全）
 */
@Tag(name = "搜索建议", description = "提供关键字联想、自动补全功能")
@Validated
@RestController
@RequestMapping("/")  // 根路径：Gateway已处理service前缀
public class SuggestController {

    private final SearchFacade searchFacade;

    public SuggestController(SearchFacade searchFacade) {
        this.searchFacade = searchFacade;
    }

    /**
     * 获取搜索建议（关键字联想）
     * <p>
     * 根据用户输入的关键词，返回相关的联想词，支持：
     * - 商品名称前缀匹配
     * - 品牌名称前缀匹配
     * - 分类名称前缀匹配
     * </p>
     *
     * @param keyword 搜索关键词（必填）
     * @param limit   返回数量限制（默认10，最大50）
     * @return 搜索建议列表
     */
    @Operation(
        summary = "获取搜索建议",
        description = "根据关键词返回联想词列表，用于前端输入框自动补全"
    )
    @GetMapping(value = "/suggest", headers = "X-API-Version=v1")
    public Result<SuggestResponseVO> suggest(
            @Parameter(description = "搜索关键词", required = true, example = "手机")
            @RequestParam("keyword") @NotBlank(message = "关键词不能为空") String keyword,
            @Parameter(description = "返回数量限制", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        SuggestQuery query = new SuggestQuery();
        query.setKeyword(keyword.trim());
        query.setLimit(limit);
        List<SuggestItemDTO> items = searchFacade.suggest(query);
        SuggestResponseVO response = new SuggestResponseVO();
        response.setItems(items);
        return Result.success(response);
    }
}
