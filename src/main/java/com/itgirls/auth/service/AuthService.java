package com.itgirls.auth.service;

import com.itgirls.auth.dto.ApiResponse;
import com.itgirls.auth.dto.ForgotPasswordRequestDTO;
import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.LoginResponseDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.ResetPasswordRequestDTO;
import com.itgirls.auth.entity.User;

public interface AuthService {
    User register(RegistrationRequestDto registrationRequestDto);
    User activateAccount(String token);
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    void logout(String refreshToken);
    ApiResponse requestPasswordReset(ForgotPasswordRequestDTO request);
    ApiResponse  resetPassword(ResetPasswordRequestDTO request, String token);
}