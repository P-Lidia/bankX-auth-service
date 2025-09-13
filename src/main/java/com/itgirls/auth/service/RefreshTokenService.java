package com.itgirls.auth.service;

import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.dto.UserJwtDto;
import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;

public interface RefreshTokenService {
    TokenResponseDto refreshTokens(String refreshToken);
    String generateAndSaveRefreshToken(User user);
}
