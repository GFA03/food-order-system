package com.omnieats.identity_service.exception;

/** Bad email/password on login — mapped to HTTP 401. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
