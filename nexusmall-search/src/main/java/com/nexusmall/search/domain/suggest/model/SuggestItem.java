package com.nexusmall.search.domain.suggest.model;

public class SuggestItem {

    private String keyword;
    private String type;
    private Double score;
    private Long count;

    public SuggestItem() {
    }

    public SuggestItem(String keyword, String type, Double score) {
        this.keyword = keyword;
        this.type = type;
        this.score = score;
    }

    public SuggestItem(String keyword, String type, Double score, Long count) {
        this.keyword = keyword;
        this.type = type;
        this.score = score;
        this.count = count;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
