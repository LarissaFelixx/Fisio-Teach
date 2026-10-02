package com.app.fisiotech.common.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageRequestFactory {
    private PageRequestFactory() {}

    public static Pageable create(int page, int size, String sort, String direction, Set<String> allowed) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String safeSort = allowed.contains(sort) ? sort : "id";
        Sort.Direction safeDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSort));
    }
}
