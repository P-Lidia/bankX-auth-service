package com.bankx.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Структура для передачи информации об ошибках через API.
 *
 * <p>Используется в контроллерах для формализованного ответа клиенту:
 * код статуса, тип ошибки, сообщение и время возникновения.
 */
@Builder
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
}