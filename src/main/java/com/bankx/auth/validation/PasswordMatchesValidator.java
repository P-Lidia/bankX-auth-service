package com.bankx.auth.validation;

import com.bankx.auth.dto.RegistrationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Валидатор для проверки совпадения пароля и его подтверждения при регистрации пользователя.
 *
 * <p>Используется вместе с аннотацией {@link PasswordMatches} на DTO регистрации.
 * Проверяет, что поля {@code password} и {@code confirmPassword} совпадают.
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegistrationRequestDto> {

    @Override
    public boolean isValid(RegistrationRequestDto requestDto, ConstraintValidatorContext context) {
        if (requestDto == null) {
            return false;
        }
        return requestDto.getPassword() != null
                && requestDto.getPassword().equals(requestDto.getConfirmPassword());
    }
}