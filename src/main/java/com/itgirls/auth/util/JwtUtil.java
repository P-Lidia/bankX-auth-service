package com.itgirls.auth.util;

import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.repository.RefreshTokenRepository;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Data
public class JwtUtil {

    private final RefreshTokenRepository refreshTokenRepository;

    public String generateAccessToken(User user) {
        return "";
    }

    @Transactional
    public RefreshToken generateAndSaveRefreshToken(User user) {
        RefreshToken refreshToken = generateRefreshToken(user);
        return saveRefreshToken(refreshToken);
    }

    private RefreshToken saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByUser(refreshToken.getUser());
        return refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken generateRefreshToken(User user) {
        return null;
    }

}
