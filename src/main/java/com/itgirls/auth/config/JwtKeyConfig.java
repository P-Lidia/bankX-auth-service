package com.itgirls.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtKeyConfig {
    public static final String RSA_ALGORITHM = "RSA";

    @Value("${jwt.private-key}")
    private String privateKey;

    @Value("${jwt.public-key}")
    private String publicKey;

    @Bean
    public PrivateKey privateKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(new X509EncodedKeySpec(keyBytes));
    }
}