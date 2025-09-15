package com.itgirls.auth.service;

import com.itgirls.auth.dto.ApiResponse;
import com.itgirls.auth.dto.ForgotPasswordRequestDTO;
import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.ResetPasswordRequestDTO;
import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.entity.User;

public interface AuthService {
    User register(RegistrationRequestDto registrationRequestDto);
    User activateAccount(String token);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    ApiResponse requestPasswordReset(ForgotPasswordRequestDTO request);
    ApiResponse resetPassword(ResetPasswordRequestDTO request, String token);
}