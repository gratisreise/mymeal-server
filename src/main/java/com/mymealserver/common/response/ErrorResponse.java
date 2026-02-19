package com.mymealserver.common.response;


import com.mymealserver.common.exception.BusinessException;
import com.mymealserver.common.exception.ErrorCode;
import com.mymealserver.common.response.classes.ErrorDetail;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse extends BaseResponse {
    private final ErrorDetail error;

    private ErrorResponse(String code, String message) {
        super(false, LocalDateTime.now());
        this.error = new ErrorDetail(code, message);
    }

    public static ErrorResponse from(ErrorCode code) {
        return new ErrorResponse(code.getCode(), code.getMessage());
    }

    public static ErrorResponse from(String code, String message) {
        return new ErrorResponse(code, message);
    }


    public static ErrorResponse unknown(RuntimeException ex) {
        ErrorCode code = ErrorCode.UNKNOWN_ERROR;
        return new ErrorResponse(code.getCode(), ex.getMessage());
    }

}