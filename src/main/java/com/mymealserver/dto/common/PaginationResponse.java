package com.mymealserver.dto.common;

public record PaginationResponse(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Boolean first,
        Boolean last
) {
}
