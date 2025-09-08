package com.itgirls.auth.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class CookieUtil {
    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/auth/refresh")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    public void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = createRefreshCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
