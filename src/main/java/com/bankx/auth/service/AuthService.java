package com.bankx.auth.service;

import com.bankx.auth.dto.ApiResponse;
import com.bankx.auth.dto.ForgotPasswordRequestDTO;
import com.bankx.auth.dto.LoginRequestDto;
import com.bankx.auth.dto.RegistrationRequestDto;
import com.bankx.auth.dto.ResetPasswordRequestDTO;
import com.bankx.auth.dto.TokenResponseDto;
import com.bankx.auth.dto.UserResponseDto;

public interface AuthService {
    ApiResponse<String> register(RegistrationRequestDto registrationRequestDto);
    ApiResponse<UserResponseDto> activateAccount(String token);
    TokenResponseDto login(LoginRequestDto loginRequestDto);
    ApiResponse<String> requestPasswordReset(ForgotPasswordRequestDTO request);
    ApiResponse<String> resetPassword(ResetPasswordRequestDTO request, String token);
}