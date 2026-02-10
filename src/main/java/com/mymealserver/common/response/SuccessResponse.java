package com.mymealserver.common.response;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class SuccessResponse<T> extends BaseResponse {
    private final T data;

    private SuccessResponse(int status, T data) {
        super(true, status, LocalDateTime.now());
        this.data = data;
    }

    //200코드 성공
    public static <T> SuccessResponse<T> from(T data) {
        return new SuccessResponse<>(200, data);
    }

    //201~2xx 성공
    public static <T> SuccessResponse<T> of(int status, T data) {
        return new SuccessResponse<>(status, data);
    }
}