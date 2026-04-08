package com.nexusmall.search.infrastructure.cache;

import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class HotSearchCacheRepository {

    public List<String> listHotKeywords() {
        return Collections.emptyList();
    }
}
