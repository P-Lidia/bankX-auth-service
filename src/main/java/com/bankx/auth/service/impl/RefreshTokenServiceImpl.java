package com.bankx.auth.service.impl;

import com.bankx.auth.dto.TokenResponseDto;
import com.bankx.auth.dto.UserJwtDto;
import com.bankx.auth.entity.RefreshToken;
import com.bankx.auth.entity.User;
import com.bankx.auth.mapper.UserMapper;
import com.bankx.auth.repository.RefreshTokenRepository;
import com.bankx.auth.service.RefreshTokenService;
import com.bankx.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


/**
 * Реализация сервиса для работы с refresh-токенами.
 *
 * <p>Предоставляет методы для:
 * <ul>
 *     <li>генерации и сохранения нового refresh-токена</li>
 *     <li>обновления access и refresh токенов</li>
 *     <li>удаления refresh-токена при logout</li>
 * </ul>
 *
 * <p>Использует {@link JwtUtil} для генерации токенов и {@link RefreshTokenRepository} для работы с БД.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    /**
     * Генерирует новый refresh-токен для пользователя и сохраняет его в базе.
     *
     * @param user пользователь, для которого создаётся refresh-токен
     * @return сгенерированный refresh-токен
     */
    @Transactional
    @Override
    public String generateAndSaveRefreshToken(User user) {

        // Генерируем новый refresh-токен
        String valueToken = jwtUtil.generateRefreshToken(userMapper.toUserJwtDto(user));

        // сохраняем новый токен в БД
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenValue(valueToken)
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtUtil.getJwtRefreshTokenExpiration()))
                .build();
        refreshTokenRepository.save(refreshToken);

        return valueToken;
    }

    /**
     * Обновляет access и refresh токены по существующему refresh-токену.
     *
     * @param refreshToken текущий refresh-токен
     * @return объект {@link TokenResponseDto} с новым access и refresh токенами
     * @throws BadCredentialsException если переданный refresh-токен недействителен
     */
    @Override
    public TokenResponseDto refreshTokens(String refreshToken) {
        jwtUtil.isValid(refreshToken);
        RefreshToken currentToken = refreshTokenRepository.findByTokenValue(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        // Генерация новых токенов
        UserJwtDto userJwtDto = userMapper.toUserJwtDto(currentToken.getUser());
        String newAccessToken = jwtUtil.generateAccessToken(userJwtDto);
        String newRefreshToken = jwtUtil.generateRefreshToken(userJwtDto);

        // Обновление refresh-токена в БД
        currentToken.setTokenValue(newRefreshToken);
        currentToken.setExpiryDate(Instant.now().plusMillis(jwtUtil.getJwtRefreshTokenExpiration()));
        refreshTokenRepository.save(currentToken);

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }

    /**
     * Удаляет refresh-токен из базы при logout пользователя.
     *
     * @param refreshToken refresh-токен пользователя
     */
    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) return;
        if (!refreshTokenRepository.existsByTokenValue(refreshToken)) {
            log.warn("Logout attempt with non-existent refresh token ");
            return;
        }
        // Удаление refresh-токена из БД
        refreshTokenRepository.deleteByTokenValue(refreshToken);
    }
}