package com.b02.peep_it.common.response;

import lombok.Getter;
import java.util.List;

@Getter
public class PagedResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final int totalPages;
    private final long totalElements;
    private final boolean hasNext;

    private PagedResponse(List<T> content, int page, int size, int totalPages, long totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.hasNext = page < totalPages - 1;
    }

    public static <T> PagedResponse<T> create(List<T> content, int page, int size, int totalPages, long totalElements) {
        return new PagedResponse<>(content, page, size, totalPages, totalElements);
    }
}