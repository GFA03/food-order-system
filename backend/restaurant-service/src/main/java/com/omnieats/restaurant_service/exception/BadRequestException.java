package com.omnieats.restaurant_service.exception;

/** Invalid client input that Bean Validation can't express declaratively — mapped to HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
