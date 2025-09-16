package com.itgirls.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String message;
    private T data; // для ДТО

    // когда нужно вернуть только сообщение без ДТО
    public ApiResponse(String message) {
        this.message = message;
        this.data = null;
    }
}