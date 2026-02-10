package com.mymealserver.common.response.classes;


import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Page;

@AllArgsConstructor
@Builder
public class Pagination {

    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;
    private boolean first;

    public static Pagination from(Page<?> page) {
        return Pagination.builder()
            .currentPage(page.getNumber() + 1) // 0-based를 1-based로 변환
            .pageSize(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .first(page.isFirst())
            .build();
    }
}
