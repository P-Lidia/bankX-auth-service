package com.itgirls.auth.service.impl;

import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.entity.EmailToken;
import com.itgirls.auth.entity.Role;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.mapper.UserMapper;
import com.itgirls.auth.repository.EmailTokenRepository;
import com.itgirls.auth.repository.RefreshTokenRepository;
import com.itgirls.auth.repository.RoleRepository;
import com.itgirls.auth.repository.UserRepository;
import com.itgirls.auth.service.AuthService;
import com.itgirls.auth.service.RefreshTokenService;
import com.itgirls.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE_ACTIVATION = "activation";
    private static final int TOKEN_EXPIRATION_DAYS = 1;

    private final UserRepository userRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public User register(RegistrationRequestDto registrationRequestDto) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(registrationRequestDto.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        // Создание пользователя через Mapper
        User user = userMapper.toEntity(registrationRequestDto, passwordEncoder);
        user.setStatus(User.Status.PENDING);
        user.setCreatedAt(LocalDateTime.now());

        Role role = roleRepository.findByCode(registrationRequestDto.getRole())
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + registrationRequestDto.getRole()));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // Генерация токена активации
        String activationToken = UUID.randomUUID().toString();

        // Сохранение токена в таблицу email_tokens
        EmailToken emailToken = EmailToken.builder()
                .userId(savedUser.getId())
                .token(activationToken)
                .expiresAt(LocalDateTime.now().plusDays(TOKEN_EXPIRATION_DAYS)) // Токен действителен 1 день
                .used(false)
                .type(TOKEN_TYPE_ACTIVATION)
                .build();

        emailTokenRepository.save(emailToken);

        // TODO: отправка события USER_REGISTERED в Kafka для Notification Service

        return savedUser;
    }

    @Override
    @Transactional
    public User activateAccount(String token) {
        EmailToken emailToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired activation token"));

        // Проверка: если токен уже использован или истек
        if (emailToken.getUsed() || emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has already been used or expired");
        }

        User user = userRepository.findById(emailToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(User.Status.ACTIVE);
        User activatedUser = userRepository.save(user);

        // Отметить токен как использованный
        emailToken.setUsed(true);
        emailTokenRepository.save(emailToken);

        // TODO: отправка события USER_ACTIVATED в Kafka для Notification Service

        return activatedUser;
    }

    @Override
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(userMapper.toUserJwtDto(user));
        String refreshToken = refreshTokenService.generateAndSaveRefreshToken(user);

        return new TokenResponseDto(
                accessToken,
                refreshToken);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (!refreshTokenRepository.existsByTokenValue(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token not found");
        }
        refreshTokenRepository.deleteByTokenValue(refreshToken);
    }
}