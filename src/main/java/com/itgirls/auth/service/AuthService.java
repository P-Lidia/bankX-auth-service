package com.itgirls.auth.service;

import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.entity.User;


import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.LoginResponseDto;

public interface AuthService {
    User register(RegistrationRequestDto registrationRequestDto);
    User activateAccount(String token);
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    void logout(String refreshToken);
}