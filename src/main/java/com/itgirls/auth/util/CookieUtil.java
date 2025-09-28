package com.itgirls.auth.util;

import com.itgirls.auth.exception.ApplicationException;
import com.itgirls.auth.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private static final String COOKIE_NAME = "refreshToken";
    public static final String SAME_SITE_STRICT = "Strict";
    private static final String COOKIE_PATH = "/";
    private static final Duration LOGOUT_MAX_AGE = Duration.ZERO;

    private final JwtUtil jwtUtil;

    public String getRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ApplicationException(ErrorCode.REFRESH_TOKEN_MISSING);
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.REFRESH_TOKEN_MISSING));
    }

    public ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // TODO: поменять флаг на true для прода
                .path(COOKIE_PATH)
                .sameSite(SAME_SITE_STRICT)
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie createRefreshCookie(String token) {
        Duration maxAge = Duration.ofMillis(jwtUtil.getJwtRefreshTokenExpiration());
        return createCookie(COOKIE_NAME, token, maxAge);
    }

    public ResponseCookie createLogoutCookie() {
        return createCookie(COOKIE_NAME, "", LOGOUT_MAX_AGE);
    }
}
