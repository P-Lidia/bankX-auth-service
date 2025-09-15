import com.itgirls.auth.entity.User;
import com.itgirls.auth.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//
//class JwtUtilTest {
//
//    private JwtUtil jwtUtil;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        jwtUtil = new JwtUtil();
//        // подставляем значения через ReflectionTestUtils, т.к. @Value в тестах не работает
//        ReflectionTestUtils.setField(jwtUtil, "jwtAccessTokenExpiration", 1000L * 60); // 1 мин
//        ReflectionTestUtils.setField(jwtUtil, "jwtRefreshTokenExpiration", 1000L * 60 * 60); // 1 час
//    }
//
//    @Test
//    void testGenerateAndValidateAccessToken() {
//        User user = User.builder().id(1L).name("testUser").build();
//        String token = jwtUtil.generateAccessToken(user, List.of("ROLE_USER"));
//
//        assertNotNull(token);
//        assertTrue(jwtUtil.isValid(token));
//        assertTrue(jwtUtil.isAccessToken(token));
//        assertEquals("testUser", jwtUtil.getSubjectFromToken(token));
//        assertEquals(1L, jwtUtil.getIdFromToken(token));
//    }
//
//    @Test
//    void testGenerateAndValidateRefreshToken() {
//        String token = jwtUtil.generateRefreshToken(2L, "john", List.of("ROLE_ADMIN"));
//
//        assertNotNull(token);
//        assertTrue(jwtUtil.isValid(token));
//        assertTrue(jwtUtil.isRefreshToken(token));
//        assertEquals("john", jwtUtil.getSubjectFromToken(token));
//        assertEquals(2L, jwtUtil.getIdFromToken(token));
//    }
//
//    @Test
//    void testExpiredTokenShouldBeInvalid() throws Exception {
//        JwtUtil shortLivedJwtUtil = new JwtUtil();
//        ReflectionTestUtils.setField(shortLivedJwtUtil, "jwtAccessTokenExpiration", 1L); // 1 мс
//        User user = User.builder().id(99L).name("temp").build();
//        String token = shortLivedJwtUtil.generateAccessToken(user, List.of("ROLE_USER"));
//
//        // подождём, чтобы точно истёк
//        Thread.sleep(10);
//
//        assertFalse(shortLivedJwtUtil.isValid(token));
//    }
//
//    @Test
//    void testInvalidTokenShouldThrow() {
//        String invalidToken = "invalid.jwt.token";
//
//        assertFalse(jwtUtil.isValid(invalidToken));
//        assertThrows(JwtException.class, () -> jwtUtil.getClaims(invalidToken));
//    }
//}