package com.bankx.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Универсальный DTO для стандартизированного ответа API.
 * <p>
 * Используется для возвращения единообразной структуры ответа
 * всем клиентам сервиса: текстовое сообщение и (опционально) полезные данные.
 *
 * @param <T> тип данных, передаваемых в поле {@code data}
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String message;
    private T data; // для ДТО

    /**
     * Конструктор для ответа только с сообщением без данных.
     *
     * @param message текстовое сообщение
     */
    public ApiResponse(String message) {
        this.message = message;
        this.data = null;
    }
}