package com.nexusmall.search.infrastructure.support.parser;

import org.springframework.stereotype.Component;

@Component
public class KeywordParser {

    public String normalize(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }
}
