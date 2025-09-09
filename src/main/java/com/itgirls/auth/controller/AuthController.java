package com.itgirls.auth.controller;

import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.service.AuthService;
import com.itgirls.auth.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        User user = authService.register(registrationRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/activate")
    public ResponseEntity<User> activate(@RequestParam String token) {
        User user = authService.activateAccount(token);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }
        if (refreshToken == null) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
        if (!jwtUtil.isValid(refreshToken)) {
            return ResponseEntity.status(401).build();
        }
        var claims = jwtUtil.getClaims(refreshToken);
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);

        User user = User.builder()
                .id(userId)
                .name(username)
                .build();

        String newAccessToken = jwtUtil.generateAccessToken(user, roles);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId,username, roles);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtUtil.getJwtRefreshTokenExpiration() / 1000)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(new TokenResponseDto(newAccessToken));

    }
    }