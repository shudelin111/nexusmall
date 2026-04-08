package com.nexusmall.search.application.command;

import java.util.ArrayList;
import java.util.List;

public class RebuildIndexCommand {

    private boolean fullRebuild;
    private List<BuildProductIndexCommand> products = new ArrayList<BuildProductIndexCommand>();

    public boolean isFullRebuild() {
        return fullRebuild;
    }

    public void setFullRebuild(boolean fullRebuild) {
        this.fullRebuild = fullRebuild;
    }

    public List<BuildProductIndexCommand> getProducts() {
        return products;
    }

    public void setProducts(List<BuildProductIndexCommand> products) {
        this.products = products;
    }
}
