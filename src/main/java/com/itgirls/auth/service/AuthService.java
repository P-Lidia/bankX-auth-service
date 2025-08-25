package com.itgirls.auth.service;

import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.entity.User;

public interface AuthService {
    User register(RegistrationRequestDto registrationRequestDto);
    User activateAccount(String token);
}