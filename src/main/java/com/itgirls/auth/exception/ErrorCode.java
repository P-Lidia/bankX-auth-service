package com.itgirls.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    EMAIL_ALREADY_TAKEN(HttpStatus.CONFLICT, "Email is already taken"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Token not found"),
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "Token is invalid or expired"),
    UNKNOWN_ROLE(HttpStatus.BAD_REQUEST,"Unknown role"),
    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "Refresh token is missing");

    private final HttpStatus httpStatus;
    private final String message;
}