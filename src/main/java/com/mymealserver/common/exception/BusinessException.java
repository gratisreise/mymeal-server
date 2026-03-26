package com.mymealserver.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode code;

  public BusinessException(ErrorCode code) {
    super(code.getMessage());
    this.code = code;
  }

  public static BusinessException error(ErrorCode code) {
    return new BusinessException(code);
  }
}
