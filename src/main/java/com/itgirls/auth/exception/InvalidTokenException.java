package com.itgirls.auth.exception;

import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException {

    private final String tokenType;
    private final Long userId;

    public InvalidTokenException(String tokenType, Long userId) {
        super("Token already used or expired");
        this.tokenType = tokenType;
        this.userId = userId;
    }
}
