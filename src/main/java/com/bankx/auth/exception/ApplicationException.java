package com.bankx.auth.exception;

import lombok.Getter;

/**
 * Кастомное исключение приложения.
 *
 * <p>Содержит код ошибки {@link ErrorCode} и опциональное пользовательское сообщение.
 * Используется для передачи структурированной информации об ошибках в сервисах.
 */
@Getter
public class ApplicationException extends RuntimeException {
    /** Код ошибки, определяющий тип и HTTP-статус */
    private final ErrorCode errorCode;
    /** Пользовательское сообщение, которое может дополнять стандартное сообщение ошибки */
    private final String customMessage;

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

    /**
     * Возвращает сообщение ошибки для этого исключения.
     *
     * <p>Если задано кастомное сообщение {@link #customMessage}, возвращается оно,
     * иначе возвращается стандартное сообщение из {@link #errorCode}.
     *
     * @return текст ошибки, который будет передан клиенту
     */
    public String getMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}