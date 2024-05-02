package com.nictas.reviews.util;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public final class PaginationUtils {

    private PaginationUtils() {
        throw new UnsupportedOperationException();
    }

    public static <T> Page<T> applyPagination(List<T> items, Pageable pageable) {
        List<T> filteredItems = getPage(items, pageable);
        return new PageImpl<>(filteredItems, pageable, items.size());
    }

    private static <T> List<T> getPage(List<T> items, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return items;
        }
        return getPage(items, pageable.getPageNumber(), pageable.getPageSize());
    }

    private static <T> List<T> getPage(List<T> items, long pageNumber, long pageSize) {
        return items.stream()
                .skip(pageSize * pageNumber)
                .limit(pageSize)
                .toList();
    }

}
