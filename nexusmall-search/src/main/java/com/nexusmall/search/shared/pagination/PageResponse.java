package com.nexusmall.search.shared.pagination;

import java.util.Collections;
import java.util.List;

public class PageResponse<T> {

    private List<T> records;
    private long total;
    private int pageNo;
    private int pageSize;

    public PageResponse() {
        this(Collections.<T>emptyList(), 0L, 1, 20);
    }

    public PageResponse(List<T> records, long total, int pageNo, int pageSize) {
        this.records = records;
        this.total = total;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
