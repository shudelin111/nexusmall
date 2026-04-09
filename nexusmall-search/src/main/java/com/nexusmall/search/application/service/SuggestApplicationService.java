package com.nexusmall.search.application.service;

import com.nexusmall.search.application.assembler.SearchAssembler;
import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.application.query.SuggestQuery;
import com.nexusmall.search.domain.suggest.service.SuggestDomainService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 搜索建议应用服务
 * <p>
 * 职责�?
 * - 提供搜索建议（关键字联想�?
 * - Redis 缓存优化（TTL 5分钟�?
 * </p>
 */
@Service
public class SuggestApplicationService {

    private final SuggestDomainService suggestDomainService;
    private final SearchAssembler searchAssembler;

    public SuggestApplicationService(SuggestDomainService suggestDomainService, SearchAssembler searchAssembler) {
        this.suggestDomainService = suggestDomainService;
        this.searchAssembler = searchAssembler;
    }

    /**
     * 获取搜索建议（带 Redis 缓存�?
     * <p>
     * 缓存策略�?
     * - Cache Name: searchSuggest
     * - Key: {keyword}:{limit}
     * - TTL: 5分钟
     * </p>
     *
     * @param query 搜索建议查询
     * @return 建议列表
     */
    @Cacheable(value = "searchSuggest", key = "#query.keyword + ':' + (#query.limit != null ? #query.limit : 10)", unless = "#result == null || #result.isEmpty()")
    public List<SuggestItemDTO> suggest(SuggestQuery query) {
        int limit = query.getLimit() == null || query.getLimit() < 1 ? 10 : query.getLimit();
        return searchAssembler.toSuggestItemDTOs(suggestDomainService.suggest(query.getKeyword(), limit));
    }
}
