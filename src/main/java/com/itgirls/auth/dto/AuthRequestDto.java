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

    @Email(message = "Email must be valid")
    private String email;
    @Size(min = 10, message = "Password must contain at least 10 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "ПPassword must contain at least one uppercase letter and one digit"
    )
    private String password;
    @Size(min = 10, message = "Password confirmation must contain at least 10 characters")
    private String confirmPassword;
}