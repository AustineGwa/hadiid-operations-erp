package com.hadiid.erp.common.web;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** Generic paged result — every list screen's repository query returns one of these. */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> items;
    private int page;       // 0-based
    private int pageSize;
    private long totalCount;

    public int getTotalPages() {
        return pageSize == 0 ? 0 : (int) Math.ceil((double) totalCount / pageSize);
    }

    public boolean isHasNext() {
        return (long) (page + 1) * pageSize < totalCount;
    }

    public boolean isHasPrevious() {
        return page > 0;
    }
}
