package com.itgirls.auth.controller;

import com.itgirls.auth.dto.LoginRequestDto;
import com.itgirls.auth.dto.RegistrationRequestDto;
import com.itgirls.auth.dto.TokenResponseDto;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.service.AuthService;
import com.itgirls.auth.service.RefreshTokenService;
import com.itgirls.auth.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

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

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        TokenResponseDto tokensDTO = authService.login(loginRequestDto);
        return buildAccessTokenResponse(tokensDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(HttpServletRequest request) {
        String refreshToken = cookieUtil.getRefreshTokenFromRequest(request);
        TokenResponseDto tokensDTO = refreshTokenService.refreshTokens(refreshToken);
        return buildAccessTokenResponse(tokensDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        authService.logout(refreshToken);
        ResponseCookie deleteCookie = cookieUtil.createLogoutCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    private ResponseEntity<String> buildAccessTokenResponse(@NonNull TokenResponseDto tokensDTO) {
        ResponseCookie refreshCookie = cookieUtil.createRefreshCookie(tokensDTO.getRefreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(tokensDTO.getAccessToken());
    }
}