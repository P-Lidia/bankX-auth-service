package com.itgirls.auth.handler;

import com.itgirls.auth.dto.ApiResponse;
import com.itgirls.auth.exception.EmailAlreadyExistsException;
import com.itgirls.auth.exception.InvalidCredentialsException;
import com.itgirls.auth.exception.InvalidTokenException;
import com.itgirls.auth.exception.RefreshTokenNotFoundException;
import com.itgirls.auth.exception.TokenNotFoundException;
import com.itgirls.auth.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse> handleTokenNotFound(TokenNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Token type={} for user id={} already used or expired",
                ex.getTokenType(), ex.getUserId());

        // Клиенту безопасное сообщение
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse("Token already used or expired"));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleEmailExists(EmailAlreadyExistsException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ApiResponse> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

     @ExceptionHandler(InvalidCredentialsException.class)
     public ResponseEntity<ApiResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex);
     }

    // global catch-all для непредусмотренных исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Internal server error"));
    }

    private ResponseEntity<ApiResponse> buildResponse(HttpStatus status, RuntimeException ex) {
        // логируем тип исключения и его сообщение
        log.warn("Exception: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
        // собираем ответ клиенту
        return ResponseEntity
                .status(status)
                .body(new ApiResponse(ex.getMessage()));
    }
}
