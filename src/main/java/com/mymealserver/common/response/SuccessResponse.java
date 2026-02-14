package com.mymealserver.common.response;

import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class SuccessResponse<T> extends BaseResponse {
    private final T data;

    private SuccessResponse(HttpStatus status, T data) {
        super(true, status.value(), LocalDateTime.now());
        this.data = data;
    }

    //200코드 성공
    public static <T> SuccessResponse<T> ok(T data) {
        return new SuccessResponse<>(HttpStatus.OK, data);
    }

    public static <T> SuccessResponse<T> created(T data) {
        return new SuccessResponse<>(HttpStatus.CREATED, data);
    }

    public static <T> SuccessResponse<T> noContent(T data) {
        return new SuccessResponse<>(HttpStatus.NO_CONTENT, data);
    }

    //201~2xx 성공
    public static <T> SuccessResponse<T> of(HttpStatus code, T data) {
        return new SuccessResponse<>(code, data);
    }
}