package com.itgirls.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDto {
    @NotBlank(message = "Name must not be blank")
    private String name;
    @NotBlank(message = "Surname must not be blank")
    private String surname;
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    private String email;
    @Size(min = 10, message = "Password must contain at least 10 characters")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter and one digit"
    )
    private String password;
    @Size(min = 10, message = "Password confirmation must contain at least 10 characters")
    private String confirmPassword;
}