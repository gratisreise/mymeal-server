package com.mymealserver.common.response;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SuccessResponse<T> extends BaseResponse {
  private final T data;

  private SuccessResponse(T data) {
    super(true, LocalDateTime.now());
    this.data = data;
  }

  private static <T> SuccessResponse<T> of(T data) {
    return new SuccessResponse<>(data);
  }

  public static <T> ResponseEntity<SuccessResponse<T>> toOk(T data) {
    return ResponseEntity.ok(of(data));
  }

  // 생성됨
  public static <T> ResponseEntity<SuccessResponse<T>> toCreated(T data) {
    return ResponseEntity.status(HttpStatus.CREATED).body(of(data));
  }

  // 응답 객체 없음
  public static <T> ResponseEntity<SuccessResponse<T>> toNoContent(T data) {
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(of(data));
  }

  // 커스텀 형식
  public static <T> ResponseEntity<SuccessResponse<T>> to(HttpStatus status, T data) {
    return ResponseEntity.status(status).body(of(data));
  }
}
