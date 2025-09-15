package com.itgirls.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class ApplicationException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String customMessage; // можно добавлять свое сообщение поверх дефолтного

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public ApplicationException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    public String getMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}