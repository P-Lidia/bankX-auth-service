package com.itgirls.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

    @Email(message = "Email должен быть валидным")
    private String email;
    @Size(min = 10, message = "Пароль должен содержать минимум 10 символов")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Пароль должен содержать хотя бы одну заглавную букву и одну цифру"
    )
    private String password;
    @Size(min = 10, message = "Подтверждение пароля должно содержать минимум 10 символов")
    private String confirmPassword;
}
