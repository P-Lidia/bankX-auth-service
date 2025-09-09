package com.itgirls.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class CookieUtil {

    private static final long REFRESH_TOKEN_MAX_AGE = Duration.ofDays(7).toSeconds();
    private static final long LOGOUT_MAX_AGE = 0;

    public ResponseCookie createCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // TODO: поменять флаг на true для прода
                .path("/auth/refresh")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie createRefreshCookie(String token) {
        return createCookie("refreshToken", token, REFRESH_TOKEN_MAX_AGE);
    }

    public ResponseCookie createLogoutCookie() {
        return createCookie("refreshToken", "", LOGOUT_MAX_AGE);
    }

}
