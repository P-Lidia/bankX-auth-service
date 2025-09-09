package com.itgirls.auth.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("Token already used or expired");
    }
}
