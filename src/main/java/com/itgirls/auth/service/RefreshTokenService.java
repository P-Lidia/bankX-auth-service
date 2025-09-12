package com.itgirls.auth.service;

import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;

public interface RefreshTokenService {
    RefreshToken generateAndSaveRefreshToken(User user);
}
