package com.nexusmall.search.interfaces.facade;

import com.nexusmall.search.application.command.BuildProductIndexCommand;
import com.nexusmall.search.application.command.RebuildIndexCommand;
import com.nexusmall.search.application.command.RemoveProductIndexCommand;
import com.nexusmall.search.application.dto.SearchResultDTO;
import com.nexusmall.search.application.dto.SuggestItemDTO;
import com.nexusmall.search.application.query.ProductSearchQuery;
import com.nexusmall.search.application.query.SuggestQuery;

import java.util.List;

public interface SearchFacade {

    SearchResultDTO search(ProductSearchQuery query);

    List<SuggestItemDTO> suggest(SuggestQuery query);

    void buildIndex(BuildProductIndexCommand command);

    void removeIndex(RemoveProductIndexCommand command);

    int rebuildIndex(RebuildIndexCommand command);
}
