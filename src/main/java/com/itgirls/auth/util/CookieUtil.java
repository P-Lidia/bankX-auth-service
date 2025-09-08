package com.itgirls.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class CookieUtil {
    public ResponseCookie createCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .sameSite("Strict")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie createRefreshCookie(String token) {
        return createCookie("refreshToken", token, Duration.ofDays(7).toSeconds());
    }

    public ResponseCookie createLogoutCookie() {
        return createCookie("refreshToken", "", 0);
    }

}
