package com.nexusmall.search.interfaces.dto;

import com.nexusmall.search.application.dto.SuggestItemDTO;

import java.util.ArrayList;
import java.util.List;

public class SuggestResponseVO {

    private List<SuggestItemDTO> items = new ArrayList<SuggestItemDTO>();

    public List<SuggestItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SuggestItemDTO> items) {
        this.items = items;
    }
}
