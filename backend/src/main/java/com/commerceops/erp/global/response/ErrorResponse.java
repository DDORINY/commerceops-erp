package com.commerceops.erp.global.response;

import com.commerceops.erp.global.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ErrorResponse {

    private final boolean success = false;
    private final int statusCode;
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;
    private final List<FieldError> errors;

    private ErrorResponse(ErrorCode errorCode, List<FieldError> errors) {
        this(errorCode, errorCode.getMessage(), errors);
    }

    private ErrorResponse(ErrorCode errorCode, String message, List<FieldError> errors) {
        this.statusCode = errorCode.getStatus();
        this.errorCode = errorCode.getCode();
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode, errors);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode, message, null);
    }

    @Getter
    public static class FieldError {
        private final String field;
        private final String value;
        private final String reason;

        public FieldError(String field, String value, String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }
    }
}
