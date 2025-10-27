package com.bankx.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для валидации соответствия пароля и его подтверждения.
 *
 * <p>Применяется к {@link com.bankx.auth.dto.RegistrationRequestDto} при регистрации пользователя и проверяет,
 * что поля {@code password} и {@code confirmPassword} совпадают.
 * Для выполнения проверки используется {@link PasswordMatchesValidator}.
 */
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordMatches {

    String message() default "Passwords do not match";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

