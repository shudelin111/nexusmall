package com.nexusmall.search.interfaces.rest;

import com.nexusmall.common.vo.Result;
import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.application.query.SuggestQuery;
import com.nexusmall.search.interfaces.dto.SuggestResponseVO;
import com.nexusmall.search.interfaces.facade.SearchFacade;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/searchs")
public class SuggestController {

    private final SearchFacade searchFacade;

    public SuggestController(SearchFacade searchFacade) {
        this.searchFacade = searchFacade;
    }

    @GetMapping(value = "/suggest", headers = "X-API-Version=v1")
    public Result<SuggestResponseVO> suggest(@RequestParam("keyword") String keyword,
                                             @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        SuggestQuery query = new SuggestQuery();
        query.setKeyword(keyword);
        query.setLimit(limit);
        List<SuggestItemDTO> items = searchFacade.suggest(query);
        SuggestResponseVO response = new SuggestResponseVO();
        response.setItems(items);
        return Result.success(response);
    }
}
