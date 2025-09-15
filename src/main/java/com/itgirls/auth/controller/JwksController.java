package com.itgirls.auth.controller;

import com.itgirls.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwtUtil jwtUtil;

    private static final String KEY_ID = "auth-service-key";
    private static final String KTY = "RSA";
    private static final String USE = "sig";
    private static final String ALG = "RS256";

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) jwtUtil.getPublicKey();

        String n = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(publicKey.getPublicExponent().toByteArray());

        Map<String, Object> jwk = Map.of(
                "kty", KTY,
                "use", USE,
                "alg", ALG,
                "kid", KEY_ID,
                "n", n,
                "e", e
        );

        return ResponseEntity.ok(Map.of("keys", List.of(jwk)));
    }

}
