package com.itgirls.auth.validation;

import com.itgirls.auth.dto.RegistrationRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

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

