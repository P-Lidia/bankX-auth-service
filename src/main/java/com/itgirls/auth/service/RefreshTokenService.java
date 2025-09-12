package com.itgirls.auth.service;

import com.itgirls.auth.dto.UserJwtDto;
import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;

public interface RefreshTokenService {
    String generateAndSaveRefreshToken(User user);
}
