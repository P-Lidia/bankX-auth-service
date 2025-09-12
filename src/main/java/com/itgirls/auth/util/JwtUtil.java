package com.itgirls.auth.util;

import com.itgirls.auth.entity.RefreshToken;
import com.itgirls.auth.entity.User;
import com.itgirls.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtUtil {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.access.lifetime}")
    private Long jwtAccessTokenExpiration;
    @Getter
    @Value("${jwt.refresh.lifetime}")
    private Long jwtRefreshTokenExpiration;

    public JwtUtil() throws Exception {
        KeyPair keyPair = generatedKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }

    private KeyPair generatedKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    public String generateAccessToken(User user, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());   // храним в токене
        claims.put("type", "access");
        claims.put("roles", roles);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getName())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtAccessTokenExpiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(Long userId, String username, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        claims.put("roles", roles);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshTokenExpiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String getSubjectFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("type").equals("access");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.get("type").equals("refresh");
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getIdFromToken(String token) {
        Number userIdNum = (Number) getClaims(token).get("userId");
        return userIdNum != null ? userIdNum.longValue() : null;
    }
}