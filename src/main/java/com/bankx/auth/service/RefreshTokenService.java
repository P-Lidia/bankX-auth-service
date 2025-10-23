package com.bankx.auth.service;

import com.bankx.auth.dto.TokenResponseDto;
import com.bankx.auth.entity.User;

public interface RefreshTokenService {
    String generateAndSaveRefreshToken(User user);
    TokenResponseDto refreshTokens(String refreshToken);
    void logout(String refreshToken);
}
