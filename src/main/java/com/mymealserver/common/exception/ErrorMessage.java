package com.mymealserver.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorMessage {


    //미처리 오류
    UNKNOWN_ERROR(500, "UNKNOWN_ERROR", null);

    private final int status;
    private final String code;
    private final String message;
}
