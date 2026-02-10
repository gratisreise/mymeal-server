package com.mymealserver.common.response;

import com.mymealserver.common.response.classes.Pagination;
import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageResponse<T> {
    private final List<T> data;
    private final Pagination pagination;

    private PageResponse(Page<T> page) {
        this.data = page.getContent();
        this.pagination = Pagination.from(page);
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page);
    }
}