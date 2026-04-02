package com.mymealserver.common.response;

import java.util.List;
import lombok.Getter;

@Getter
public class CursorPageResponse<T> {

  private final List<T> data;
  private final Long nextCursor;
  private final boolean hasNext;
  private final int size;

  private CursorPageResponse(List<T> data, Long nextCursor, boolean hasNext, int size) {
    this.data = data;
    this.nextCursor = nextCursor;
    this.hasNext = hasNext;
    this.size = size;
  }

  public static <T> CursorPageResponse<T> of(
      List<T> data, Long nextCursor, boolean hasNext, int size) {
    return new CursorPageResponse<>(data, nextCursor, hasNext, size);
  }
}
