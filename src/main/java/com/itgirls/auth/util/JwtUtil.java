package com.itgirls.auth.util;

import com.itgirls.auth.dto.UserJwtDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String CLAIMS_USER_ID = "userId";
    private static final String CLAIMS_TOKEN_TYPE = "type";
    private static final String CLAIMS_USER_ROLE = "roles";

    private final PrivateKey privateKey;
    @Getter
    private final PublicKey publicKey;

    @Value("${jwt.access.lifetime}")
    private long jwtAccessTokenExpiration;
    @Getter
    @Value("${jwt.refresh.lifetime}")
    private long jwtRefreshTokenExpiration;

    public String generateAccessToken(UserJwtDto userJwtDto) {
        return generateToken(userJwtDto, TOKEN_TYPE_ACCESS, jwtAccessTokenExpiration);
    }

    public String generateRefreshToken(UserJwtDto userJwtDto) {
        return generateToken(userJwtDto, TOKEN_TYPE_REFRESH, jwtRefreshTokenExpiration);
    }

    private String generateToken(UserJwtDto userJwtDto, String tokenType, long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIMS_USER_ID, userJwtDto.getId());
        claims.put(CLAIMS_TOKEN_TYPE, tokenType);
        claims.put(CLAIMS_USER_ROLE, userJwtDto.getRole());
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userJwtDto.getName())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid JWT token", e);
        }
    }

    public String getSubjectFromToken(String token) {
        String subject = getClaims(token).getSubject();
        if (subject != null && !subject.isBlank()) {
            return subject;
        }
        throw new BadCredentialsException("Invalid JWT token");
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get(CLAIMS_TOKEN_TYPE).equals(TOKEN_TYPE_ACCESS);
        } catch (JwtException | NullPointerException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get(CLAIMS_TOKEN_TYPE).equals(TOKEN_TYPE_REFRESH);
        } catch (JwtException | NullPointerException e) {
            return false;
        }
    }

    public Long getIdFromToken(String token) {
        Long userId = (Long) getClaims(token).get(CLAIMS_USER_ID);
        if (userId != null) {
            return userId;
        }
        throw new BadCredentialsException("Invalid JWT token");
    }
}