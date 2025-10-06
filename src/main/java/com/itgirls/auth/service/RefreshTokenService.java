package com.itgirls.auth.service;

import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.entity.User;

public interface RefreshTokenService {
    String generateAndSaveRefreshToken(User user);
    TokenResponseDto refreshTokens(String refreshToken);
    void logout(String refreshToken);
}
