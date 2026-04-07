package com.nexusmall.search.infrastructure.support.tokenizer;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class KeywordTokenizer {

    public List<String> tokenize(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(keyword.trim().split("\\s+"));
    }
}
