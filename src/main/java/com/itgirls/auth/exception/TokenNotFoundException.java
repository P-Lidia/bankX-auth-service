package com.itgirls.auth.exception;

public class TokenNotFoundException extends RuntimeException {

    public TokenNotFoundException() {
        super("Token not found");
    }
}