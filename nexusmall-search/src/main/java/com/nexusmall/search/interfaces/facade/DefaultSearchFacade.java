package com.nexusmall.search.interfaces.facade;

import com.nexusmall.search.application.command.BuildProductIndexCommand;
import com.nexusmall.search.application.command.RebuildIndexCommand;
import com.nexusmall.search.application.command.RemoveProductIndexCommand;
import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.application.query.ProductSearchQuery;
import com.nexusmall.search.application.query.SuggestQuery;
import com.nexusmall.search.application.service.IndexApplicationService;
import com.nexusmall.search.application.service.SearchApplicationService;
import com.nexusmall.search.application.service.SuggestApplicationService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultSearchFacade implements SearchFacade {

    private final SearchApplicationService searchApplicationService;
    private final SuggestApplicationService suggestApplicationService;
    private final IndexApplicationService indexApplicationService;

    public DefaultSearchFacade(SearchApplicationService searchApplicationService,
                               SuggestApplicationService suggestApplicationService,
                               IndexApplicationService indexApplicationService) {
        this.searchApplicationService = searchApplicationService;
        this.suggestApplicationService = suggestApplicationService;
        this.indexApplicationService = indexApplicationService;
    }

    @Override
    public SearchResultDTO search(ProductSearchQuery query) {
        return searchApplicationService.search(query);
    }

    @Override
    public List<SuggestItemDTO> suggest(SuggestQuery query) {
        return suggestApplicationService.suggest(query);
    }

    @Override
    public void buildIndex(BuildProductIndexCommand command) {
        indexApplicationService.buildProductIndex(command);
    }

    @Override
    public void removeIndex(RemoveProductIndexCommand command) {
        indexApplicationService.removeProductIndex(command);
    }

    @Override
    public int rebuildIndex(RebuildIndexCommand command) {
        return indexApplicationService.rebuildIndex(command);
    }
}
