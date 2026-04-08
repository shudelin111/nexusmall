package com.nexusmall.search.domain.search.model;

import java.util.ArrayList;
import java.util.List;

public class Facet {

    private String name;
    private List<FacetValue> values = new ArrayList<FacetValue>();

    public Facet() {
    }

    public Facet(String name, List<FacetValue> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FacetValue> getValues() {
        return values;
    }

    public void setValues(List<FacetValue> values) {
        this.values = values;
    }

    public static class FacetValue {
        private String label;
        private long count;

        public FacetValue() {
        }

        public FacetValue(String label, long count) {
            this.label = label;
            this.count = count;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
