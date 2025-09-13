package com.itgirls.auth.service.impl;

import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.dto.UserJwtDto;
import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.mapper.UserMapper;
import com.itgirls.auth.repository.RefreshTokenRepository;
import com.itgirls.auth.service.RefreshTokenService;
import com.itgirls.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public TokenResponseDto refreshTokens(String refreshToken) {
        jwtUtil.isValid(refreshToken);
        RefreshToken tokenEntity = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        User user = tokenEntity.getUser();

        UserJwtDto userJwtDto = userMapper.toUserJwtDto(user);
        String newAccessToken = jwtUtil.generateAccessToken(userJwtDto);
        String newRefreshToken = generateAndSaveRefreshToken(user);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    @Transactional
    @Override
    public String generateAndSaveRefreshToken(User user) {

        String valueToken = jwtUtil.generateRefreshToken(userMapper.toUserJwtDto(user));

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenValue(valueToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getJwtRefreshTokenExpiration()))
                .build();
        saveRefreshToken(refreshToken);
        return valueToken;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) return;
        if (!refreshTokenRepository.existsByTokenValue(refreshToken)) {
            log.warn("Logout attempt with non-existent refresh token");
            return;
        }
        refreshTokenRepository.deleteByTokenValue(refreshToken);
    }

    private void saveRefreshToken(RefreshToken refreshToken) {
        refreshTokenRepository.deleteByUser(refreshToken.getUser());
        refreshTokenRepository.save(refreshToken);
    }
}