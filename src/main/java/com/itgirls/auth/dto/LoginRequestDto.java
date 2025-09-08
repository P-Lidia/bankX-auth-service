package com.itgirls.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {


    @NotBlank(message = "Field cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Field cannot be empty")
    @Size(min = 10, message = "Password must be at least 10 characters long")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).+$",
            message = "Password must contain at least one uppercase letter, one digit and one special character"
    )
    private String password;
}