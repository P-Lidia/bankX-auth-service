package com.itgirls.auth.util;

import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;

public class JwtUtil {
    public String generateAccessToken(User user) {
        return "";
    }

    public RefreshToken generateRefreshToken(User user) {
        return null;
    }

    public void revokeRefreshToken(String refreshToken) {
    }

    public void saveRefreshToken(RefreshToken refreshToken) {

    }
}
