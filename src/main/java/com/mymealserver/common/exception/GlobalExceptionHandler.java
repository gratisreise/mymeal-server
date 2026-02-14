package com.mymealserver.common.exception;

import com.mymealserver.common.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> businessExceptionHandler(BusinessException ex) {
        ErrorCode errorCode = ex.getCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // 처리되지 않은 오류
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> missingExceptionHandler(RuntimeException ex) {
        return ResponseEntity
                .status(ErrorCode.UNKNOWN_ERROR.getStatus())
                .body(ErrorResponse.unknown(ex));
    }
}
