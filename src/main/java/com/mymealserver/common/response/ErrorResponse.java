package com.mymealserver.common.response;


import com.mymealserver.common.exception.ErrorMessage;
import com.mymealserver.common.response.classes.ErrorDetail;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ErrorResponse extends BaseResponse {
    private final ErrorDetail error;

    private ErrorResponse(int status, String code, String message) {
        super(false, status, LocalDateTime.now());
        this.error = new ErrorDetail(code, message);
    }

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message);
    }

    public static ErrorResponse unknown(RuntimeException ex){
        ErrorMessage message = ErrorMessage.UNKNOWN_ERROR;
        return new ErrorResponse(message.getStatus(), message.getCode(), ex.getMessage());
    }

}