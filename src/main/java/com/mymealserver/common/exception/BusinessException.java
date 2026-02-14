package com.mymealserver.common.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(ErrorMessage message) {
        super(message.name());
    }
}
