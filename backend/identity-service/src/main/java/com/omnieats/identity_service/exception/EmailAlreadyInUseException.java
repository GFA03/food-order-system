package com.omnieats.identity_service.exception;

/** Registration with an email that already exists — mapped to HTTP 409. */
public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
