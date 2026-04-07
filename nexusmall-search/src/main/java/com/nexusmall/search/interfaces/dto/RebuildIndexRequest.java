package com.nexusmall.search.interfaces.dto;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class RebuildIndexRequest {

    @NotNull(message = "fullRebuild must not be null")
    private Boolean fullRebuild;
    private List<IndexProductRequest> products = new ArrayList<IndexProductRequest>();

    public Boolean getFullRebuild() {
        return fullRebuild;
    }

    public void setFullRebuild(Boolean fullRebuild) {
        this.fullRebuild = fullRebuild;
    }

    public List<IndexProductRequest> getProducts() {
        return products;
    }

    public void setProducts(List<IndexProductRequest> products) {
        this.products = products;
    }
}
