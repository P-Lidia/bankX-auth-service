package com.itgirls.auth.handler;

import com.itgirls.auth.exception.ApplicationException;
import com.itgirls.auth.exception.ErrorCode;
import com.itgirls.auth.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_VALIDATION = "VALIDATION_ERROR";
    private static final String ERROR_INTERNAL = "INTERNAL_ERROR";
    private static final String ERROR_INTERNAL_MESSAGE = "Unexpected error occurred";
    private static final int FIRST_ERROR = 0;

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponse> handleApplicationException(ApplicationException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        ErrorResponse response = ErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .error(errorCode.name())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    // Ошибки валидации @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        // из всех ошибок при валидации возьмет первую и покажет с дефолтным сообщением из ДТО
        String message = ex.getBindingResult().getAllErrors().get(FIRST_ERROR).getDefaultMessage();

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(ERROR_VALIDATION)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // fallback для неожиданных ошибок
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(ERROR_INTERNAL)
                .message(ERROR_INTERNAL_MESSAGE)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.internalServerError().body(response);
    }
}