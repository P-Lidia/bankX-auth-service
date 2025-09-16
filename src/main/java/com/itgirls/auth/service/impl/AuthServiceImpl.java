package com.itgirls.auth.service.impl;

import com.itgirls.auth.dto.ApiResponse;
import com.itgirls.auth.dto.ForgotPasswordRequestDTO;
import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.ResetPasswordRequestDTO;
import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.dto.UserEventDto;
import com.itgirls.auth.dto.UserResponseDto;
import com.itgirls.auth.entity.EmailToken;
import com.itgirls.auth.entity.Role;
import com.itgirls.auth.entity.User;
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
import org.springframework.security.authentication.BadCredentialsException;
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
    public ApiResponse<String> register(RegistrationRequestDto registrationRequestDto) {
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

        // Генерация токена активации и сохранение токена в таблицу email_tokens
        String activationToken = generateToken(savedUser, TOKEN_TYPE_ACTIVATION);

        //sending registration event to Kafka
        UserEventDto userEventDto = createUserEventDto(savedUser, activationToken);
        kafkaProducer.sendRegistrationEvent(savedUser.getId().toString(), userEventDto);

        return new ApiResponse<>("Registration successful, check your email");
    }

    @Override
    @Transactional
    public ApiResponse<UserResponseDto> activateAccount(String token) {
        // Поиск токена и проверка: если токен уже использован или истек
        EmailToken emailToken = findAndValidateEmailToken(token);

        User user = findUserById(emailToken.getUserId());
        user.setStatus(User.Status.ACTIVE);
        User activatedUser = userRepository.save(user);

        // Отметить токен как использованный
        emailToken.setUsed(true);
        emailTokenRepository.save(emailToken);

        return new ApiResponse<>(
                "Account successfully activated",
                userMapper.toUserResponseDto(activatedUser)
        );
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
    public ApiResponse<String> requestPasswordReset(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    return new UserNotFoundByEmailException();
                });
        String resetToken = generateToken(user, TOKEN_TYPE_RECOVERY_PASSWORD);

        //sending reset password event to Kafka
        UserEventDto userEventDto = createUserEventDto(user, resetToken);
        kafkaProducer.sendResetPasswordEvent(user.getId().toString(), userEventDto);

        log.info("Password reset token generated for user id={}", user.getId());
        return new ApiResponse<>("Password reset link sent, check your email");
    }

    @Override
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequestDTO request, String token) {
        EmailToken emailToken = findAndValidateEmailToken(token);

        User user = findUserById(emailToken.getUserId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        emailToken.setUsed(true);
        emailTokenRepository.save(emailToken);

        log.info("New password for user id={} saved", user.getId());
        return new ApiResponse<>("Password successfully saved");
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    return new UserNotFoundByIdException(userId);
                });
    }

    private EmailToken findAndValidateEmailToken(String token) {
        EmailToken emailToken = emailTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    return new TokenNotFoundException();
                });

        // Проверка: если токен уже использован или истек
        if (emailToken.getUsed() || emailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Token type={} for user id={} already used or expired",
                    emailToken.getType(), emailToken.getUserId());
            throw new InvalidTokenException(emailToken.getType(), emailToken.getUserId());
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

    private UserEventDto createUserEventDto(User user, String emailToken) {
        return UserEventDto.builder()
                .firstName(user.getName())
                .lastName(user.getSurname())
                .email(user.getEmail())
                .emailToken(emailToken)
                .build();
    }
}