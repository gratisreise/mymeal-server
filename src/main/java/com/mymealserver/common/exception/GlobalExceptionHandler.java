package com.mymealserver.common.exception;

import com.mymealserver.common.response.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {




    // 처리돼지 않은 오류
    @ExceptionHandler(RuntimeException.class)
    public ErrorResponse missingExceptionHandler(RuntimeException ex){
        return ErrorResponse.unknown(ex);
    }
}
