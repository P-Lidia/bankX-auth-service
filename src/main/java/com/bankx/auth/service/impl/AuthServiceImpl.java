package com.bankx.auth.service.impl;

import com.bankx.auth.dto.ApiResponse;
import com.bankx.auth.dto.ForgotPasswordRequestDTO;
import com.bankx.auth.dto.LoginRequestDto;
import com.bankx.auth.dto.RegistrationRequestDto;
import com.bankx.auth.dto.ResetPasswordRequestDTO;
import com.bankx.auth.dto.TokenResponseDto;
import com.bankx.auth.dto.UserEventDto;
import com.bankx.auth.dto.UserResponseDto;
import com.bankx.auth.entity.EmailToken;
import com.bankx.auth.entity.Role;
import com.bankx.auth.entity.User;
import com.bankx.auth.exception.ApplicationException;
import com.bankx.auth.exception.ErrorCode;
import com.bankx.auth.kafka.producer.KafkaProducer;
import com.bankx.auth.mapper.UserMapper;
import com.bankx.auth.repository.EmailTokenRepository;
import com.bankx.auth.repository.RoleRepository;
import com.bankx.auth.repository.UserRepository;
import com.bankx.auth.service.AuthService;
import com.bankx.auth.service.RefreshTokenService;
import com.bankx.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сервис для аутентификации и управления пользователями.
 *
 * <p>Предоставляет методы для:
 * <ul>
 *     <li>регистрации нового пользователя</li>
 *     <li>активации аккаунта по email-токену</li>
 *     <li>логина с генерацией access и refresh jwt-токенов</li>
 *     <li>запроса сброса пароля и его обновления</li>
 * </ul>
 *
 * <p>Использует:
 * <ul>
 *     <li>{@link UserRepository} для работы с пользователями</li>
 *     <li>{@link EmailTokenRepository} для управления email-токенами</li>
 *     <li>{@link RoleRepository} для назначения ролей</li>
 *     <li>{@link JwtUtil} для генерации JWT</li>
 *     <li>{@link RefreshTokenService} для работы с refresh-токенами</li>
 *     <li>{@link PasswordEncoder} для шифрования пароля</li>
 *     <li>{@link KafkaProducer} для отправки событий в Kafka</li>
 * </ul>
 */
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

    /**
     * Регистрирует нового пользователя.
     *
     * <p>Проверяет уникальность email, назначает роль, сохраняет пользователя,
     * генерирует токен активации и отправляет событие в Kafka.
     *
     * @param registrationRequestDto DTO с данными для регистрации
     * @return {@link ApiResponse} с сообщением о результате регистрации
     * @throws ApplicationException если email уже занят или роль неизвестна
     */
    @Override
    @Transactional
    public ApiResponse<String> register(RegistrationRequestDto registrationRequestDto) {
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
        UserEventDto userEventDto = createUserEventDto(savedUser, activationToken);
        kafkaProducer.sendRegistrationEvent(savedUser.getId().toString(), userEventDto);

        return new ApiResponse<>("Registration successful, check your email");
    }

    /**
     * Активирует аккаунт пользователя по email-токену активации.
     *
     * @param token email-токен активации
     * @return {@link ApiResponse} с сообщением и данными пользователя
     * @throws ApplicationException если email-токен недействителен или пользователь не найден
     */
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

    /**
     * Логин пользователя с генерацией access и refresh jwt-токенов.
     *
     * @param loginRequestDto DTO с email и паролем
     * @return {@link TokenResponseDto} с access и refresh jwt-токенами
     * @throws ApplicationException если учетные данные неверны
     */
    @Override
    public TokenResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCode.INVALID_CREDENTIALS,
                        "Invalid login attempt with email: " + loginRequestDto.getEmail()
                ));
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtil.generateAccessToken(userMapper.toUserJwtDto(user));
        String refreshToken = refreshTokenService.generateAndSaveRefreshToken(user);

        return new TokenResponseDto(
                accessToken,
                refreshToken);
    }

    /**
     * Запрашивает сброс пароля для пользователя по email.
     *
     * <p>Генерирует email-токен восстановления и отправляет событие в Kafka.
     *
     * @param request DTO с email пользователя
     * @return {@link ApiResponse} с сообщением о ссылке для сброса
     * @throws ApplicationException если пользователь с указанным email не найден
     */
    @Override
    public ApiResponse<String> requestPasswordReset(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("User not found with email=%s", request.getEmail())
                ));
        String resetToken = generateToken(user, TOKEN_TYPE_RECOVERY_PASSWORD);

        //sending reset password event to Kafka
        UserEventDto userEventDto = createUserEventDto(user, resetToken);
        kafkaProducer.sendResetPasswordEvent(user.getId().toString(), userEventDto);

        log.info("Password reset token generated for user id={}", user.getId());
        return new ApiResponse<>("Password reset link sent, check your email");
    }

    /**
     * Сбрасывает пароль пользователя по email-токену восстановления.
     *
     * <p>Обновляет пароль пользователя и помечает email-токен как использованный.
     *
     * @param request DTO с новым паролем
     * @param token email-токен восстановления
     * @return {@link ApiResponse} с сообщением об успешном изменении пароля
     * @throws ApplicationException если email-токен недействителен или пользователь не найден
     */
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
                .orElseThrow(() -> new ApplicationException(
                        ErrorCode.USER_NOT_FOUND,
                        String.format("User not found with id=%d", userId)
                ));
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

    /**
     * Генерирует email-токен и сохраняет его в базе.
     */
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

    /**
     * Создаёт DTO события для отправки в Kafka.
     */
    private UserEventDto createUserEventDto(User user, String emailToken) {
        return UserEventDto.builder()
                .firstName(user.getName())
                .lastName(user.getSurname())
                .email(user.getEmail())
                .emailToken(emailToken)
                .build();
    }
}