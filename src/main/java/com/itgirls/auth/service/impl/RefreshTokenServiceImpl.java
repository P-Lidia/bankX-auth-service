package com.itgirls.auth.service.impl;

import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.repository.RefreshTokenRepository;
import com.itgirls.auth.service.RefreshTokenService;
import com.itgirls.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public RefreshToken generateAndSaveRefreshToken(User user) {
        String valueToken = jwtUtil.generateRefreshToken(user);
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenValue(valueToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getJwtRefreshTokenExpiration()))
                .build();
        return saveRefreshToken(refreshToken);
    }

    private RefreshToken saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByUser(refreshToken.getUser());
        return refreshTokenRepository.save(refreshToken);
    }
}
