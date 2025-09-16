package com.itgirls.auth.service;

import com.itgirls.auth.dto.ApiResponse;
import com.itgirls.auth.dto.ForgotPasswordRequestDTO;
import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.ResetPasswordRequestDTO;
import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.dto.UserResponseDto;

public interface AuthService {
    ApiResponse<String> register(RegistrationRequestDto registrationRequestDto);
    ApiResponse<UserResponseDto> activateAccount(String token);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    ApiResponse<String> requestPasswordReset(ForgotPasswordRequestDTO request);
    ApiResponse<String> resetPassword(ResetPasswordRequestDTO request, String token);
}