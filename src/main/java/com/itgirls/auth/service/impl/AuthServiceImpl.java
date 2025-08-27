package com.itgirls.auth.service.impl;

import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.LoginResponseDto;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.repository.UserRepository;
import com.itgirls.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private LoginRequestDto loginRequestDto;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        this.loginRequestDto = loginRequestDto;
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Неверный email"));

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Неверный пароль");
        }
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        return new LoginResponseDto(
                accessToken,
                refreshToken
        );
    }

    public void logout(String refreshToken) {
        jwtUtil.revokeRefreshToken(refreshToken);
    }
}