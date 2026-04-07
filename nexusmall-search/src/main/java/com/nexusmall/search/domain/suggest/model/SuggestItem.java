package com.nexusmall.search.domain.suggest.model;

public class SuggestItem {

    private String keyword;
    private String type;
    private Double score;

    public SuggestItem() {
    }

    public SuggestItem(String keyword, String type, Double score) {
        this.keyword = keyword;
        this.type = type;
        this.score = score;
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
}
