package com.bankx.auth.util;

import com.bankx.auth.exception.ApplicationException;
import com.bankx.auth.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

/**
 * Утилитарный компонент для работы с HTTP Cookie.
 *
 * <p>Предоставляет методы для:
 * - извлечения refresh-токена из запроса,
 * - создания безопасных HttpOnly cookie,
 * - генерации cookie для refresh-токена и logout.
 */

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private static final String COOKIE_NAME = "refreshToken";
    public static final String SAME_SITE_STRICT = "Strict";
    private static final String COOKIE_PATH = "/";
    private static final Duration LOGOUT_MAX_AGE = Duration.ZERO;

    private final JwtUtil jwtUtil;

    /**
     * Извлекает refresh-токен из cookie запроса.
     *
     * @param request HTTP-запрос, содержащий cookie
     * @return значение refresh-токена
     * @throws ApplicationException если cookie отсутствует или refresh-токен не найден
     */
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

    /**
     * Создаёт HTTP cookie с заданными параметрами.
     *
     * @param name имя cookie
     * @param value значение cookie
     * @param maxAge время жизни cookie
     * @return объект ResponseCookie с указанными настройками
     */
    public ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // TODO: поменять флаг на true для прода
                .path(COOKIE_PATH)
                .sameSite(SAME_SITE_STRICT)
                .maxAge(maxAge)
                .build();
    }

        /**
     * Создаёт HttpOnly cookie для refresh-токена пользователя.
     *
     * <p>Срок жизни cookie совпадает со сроком действия refresh-токена,
     * который возвращается методом {@link JwtUtil#getJwtRefreshTokenExpiration()}.
     *
     * @param token refresh-токен
     * @return объект ResponseCookie с refresh-токеном и корректным сроком жизни
     */
    public ResponseCookie createRefreshCookie(String token) {
        Duration maxAge = Duration.ofMillis(jwtUtil.getJwtRefreshTokenExpiration());
        return createCookie(COOKIE_NAME, token, maxAge);
    }

    /**
     * Создаёт cookie для logout пользователя.
     * <p>Cookie имеет пустое значение и {@code maxAge = 0}, чтобы удалить refresh-токен из браузера.
     *
     * @return объект ResponseCookie для удаления refresh-cookie
     */
    public ResponseCookie createLogoutCookie() {
        return createCookie(COOKIE_NAME, "", LOGOUT_MAX_AGE);
    }
}
