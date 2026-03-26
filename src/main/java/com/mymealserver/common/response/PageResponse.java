package com.mymealserver.common.response;

import java.util.List;
import lombok.Builder;
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

  @Builder
  public record Pagination(
      int currentPage,
      int pageSize,
      long totalElements,
      int totalPages,
      boolean last,
      boolean first) {
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
}
