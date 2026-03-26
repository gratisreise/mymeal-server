package com.mymealserver.common.exception;

import com.mymealserver.common.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    String message = ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();

    return ResponseEntity.status(status).body(ErrorResponse.from(status.name(), message));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    String message =
        ex.getConstraintViolations().stream()
            .findFirst()
            .map(ConstraintViolation::getMessage)
            .orElse("Invalid parameter value");

    return ResponseEntity.status(status).body(ErrorResponse.from(status.name(), message));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    String message =
        String.format("Invalid parameter value for '%s': %s", ex.getName(), ex.getValue());

    return ResponseEntity.status(status).body(ErrorResponse.from(status.name(), message));
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> businessExceptionHandler(BusinessException ex) {
    ErrorCode errorCode = ex.getCode();
    return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.from(errorCode));
  }

  // 처리되지 않은 오류
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> missingExceptionHandler(Exception ex) {
    return ResponseEntity.status(ErrorCode.UNKNOWN_ERROR.getStatus())
        .body(ErrorResponse.unknown(ex));
  }
}
