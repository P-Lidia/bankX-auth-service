package com.itgirls.auth.service.impl;

import com.itgirls.auth.dto.ApiResponse;
import com.itgirls.auth.dto.ForgotPasswordRequestDTO;
import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.ResetPasswordRequestDTO;
import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.dto.UserEventDto;
import com.itgirls.auth.entity.EmailToken;
import com.itgirls.auth.entity.Role;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.exception.ApplicationException;
import com.itgirls.auth.exception.ErrorCode;
import com.itgirls.auth.kafka.producer.KafkaProducer;
import com.itgirls.auth.mapper.UserMapper;
import com.itgirls.auth.repository.EmailTokenRepository;
import com.itgirls.auth.repository.RoleRepository;
import com.itgirls.auth.repository.UserRepository;
import com.itgirls.auth.service.AuthService;
import com.itgirls.auth.service.RefreshTokenService;
import com.itgirls.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE_ACTIVATION = "activation";
    private static final String TOKEN_TYPE_RECOVERY_PASSWORD = "recovery_password";
    private static final int TOKEN_EXPIRATION_DAYS = 1;

    private final UserRepository userRepository;
    private final EmailTokenRepository emailTokenRepository;
    private final RoleRepository roleRepository;

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final KafkaProducer kafkaProducer;

    @Override
    @Transactional
    public User register(RegistrationRequestDto registrationRequestDto) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(registrationRequestDto.getEmail())) {
            throw new ApplicationException(ErrorCode.EMAIL_ALREADY_TAKEN);
        }

        // Создание пользователя через Mapper
        User user = userMapper.toEntity(registrationRequestDto, passwordEncoder);
        user.setStatus(User.Status.PENDING);
        user.setCreatedAt(LocalDateTime.now());

        Role role = roleRepository.findByCode(registrationRequestDto.getRole())
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNKNOWN_ROLE));
        user.setRole(role);

        User savedUser = userRepository.save(user);

        // Генерация токена активации и сохранение токена в таблицу email_tokens
        String activationToken = generateToken(savedUser, TOKEN_TYPE_ACTIVATION);

        //sending registration event to Kafka
        UserEventDto userEventDto = UserEventDto.builder()
                .firstName(savedUser.getName())
                .lastName(savedUser.getSurname())
                .email(savedUser.getEmail())
                .activationKey(activationToken)
                .build();
        kafkaProducer.sendRegistrationEvent(savedUser.getId().toString(), userEventDto);

        return savedUser;
    }

    @Override
    @Transactional
    public User activateAccount(String token) {
        // Поиск токена и проверка: если токен уже использован или истек
        EmailToken emailToken = findAndValidateEmailToken(token);

        User user = findUserById(emailToken.getUserId());
        user.setStatus(User.Status.ACTIVE);
        User activatedUser = userRepository.save(user);

        // Отметить токен как использованный
        emailToken.setUsed(true);
        emailTokenRepository.save(emailToken);

        return activatedUser;
    }

    @Override
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateAccessToken(userMapper.toUserJwtDto(user));
        String refreshToken = refreshTokenService.generateAndSaveRefreshToken(user);

        return new TokenResponseDto(
                accessToken,
                refreshToken);
    }

    @Override
    public ApiResponse requestPasswordReset(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
        String activationToken = generateToken(user, TOKEN_TYPE_RECOVERY_PASSWORD);

        // TODO: отправка события в Kafka notifications.reset.password.events

        log.info("Password reset token generated for user id={}", user.getId());
        return new ApiResponse("Password reset link sent");
    }

    @Override
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequestDTO request, String token) {
        EmailToken emailToken = findAndValidateEmailToken(token);

        User user = findUserById(emailToken.getUserId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        emailToken.setUsed(true);
        emailTokenRepository.save(emailToken);

        log.info("New password for user id={} saved", user.getId());
        return new ApiResponse("Password successfully saved");
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));
    }

    private EmailToken findAndValidateEmailToken(String token) {
        EmailToken emailToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> new ApplicationException(ErrorCode.TOKEN_NOT_FOUND));

        // Проверка: если токен уже использован или истек
        if (emailToken.getUsed() || emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Token type={} for user id={} already used or expired",
                    emailToken.getType(), emailToken.getUserId());
            throw new ApplicationException(ErrorCode.TOKEN_INVALID);
        }
        return emailToken;
    }

    private String generateToken(User user, String type) {
        // Генерация токена
        String token = UUID.randomUUID().toString();

        // Сохранение токена в таблицу email_tokens
        EmailToken emailToken = EmailToken.builder()
                .userId(user.getId())
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(TOKEN_EXPIRATION_DAYS)) // Токен действителен 1 день
                .used(false)
                .type(type)
                .build();
        emailTokenRepository.save(emailToken);

        log.info("EmailToken of type={} generated for user id={}, expires at {}",
                type, user.getId(), emailToken.getExpiresAt());
        return token;
    }
}