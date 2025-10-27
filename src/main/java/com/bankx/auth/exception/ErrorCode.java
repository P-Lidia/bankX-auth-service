package com.bankx.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Enum с кодами ошибок приложения.
 *
 * <p>Каждое значение содержит:
 * <ul>
 *     <li>HTTP-статус ответа</li>
 *     <li>Стандартное сообщение ошибки</li>
 * </ul>
 *
 * <p>Используется для:
 * <ul>
 *     <li>{@link ApplicationException} – для генерации исключений с кодом ошибки</li>
 *     <li>{@link ErrorResponse} – для заполнения поля {@code error} и HTTP-статуса в ответе API</li>
 * </ul>
 */
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