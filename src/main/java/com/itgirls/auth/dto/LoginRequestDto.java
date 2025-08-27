package com.itgirls.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class LoginRequestDto {

    @Getter
    @NotBlank(message = "Поле не может быть пустым")
    @Email(message = "Email должен быть валидным")
    private String email;

    @Getter
    @NotBlank(message = "Поле не может быть пустым")
    @Size(min = 10, message = "Пароль должен содержать минимум 10 символов")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
            message = "Пароль должен содержать минимум: 1 заглавную букву, 1 цифру, 1 специальный символ"
    )
    private String password;
}
