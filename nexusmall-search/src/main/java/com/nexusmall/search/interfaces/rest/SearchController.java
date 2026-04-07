package com.nexusmall.search.interfaces.rest;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.application.query.ProductSearchQuery;
import com.nexusmall.search.interfaces.dto.SearchRequestVO;
import com.nexusmall.search.interfaces.dto.SearchResponseVO;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/searchs")
public class SearchController {

    private final SearchFacade searchFacade;

    public SearchController(SearchFacade searchFacade) {
        this.searchFacade = searchFacade;
    }

    @GetMapping(value = "/products", headers = "X-API-Version=v1")
    public Result<SearchResponseVO> searchProducts(@Validated @ModelAttribute SearchRequestVO request) {
        ProductSearchQuery query = new ProductSearchQuery();
        BeanUtils.copyProperties(request, query);
        SearchResultDTO result = searchFacade.search(query);
        SearchResponseVO response = new SearchResponseVO();
        BeanUtils.copyProperties(result, response);
        return Result.success(response);
    }
}
