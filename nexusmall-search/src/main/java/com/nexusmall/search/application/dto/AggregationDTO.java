package com.nexusmall.search.application.dto;

import java.util.ArrayList;
import java.util.List;

public class AggregationDTO {

    private String name;
    private List<BucketDTO> buckets = new ArrayList<BucketDTO>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BucketDTO> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketDTO> buckets) {
        this.buckets = buckets;
    }

    public static class BucketDTO {
        private String label;
        private long count;

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
