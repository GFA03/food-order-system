package com.omnieats.restaurant_service.exception;

/** Base type for "entity not found" cases — mapped to HTTP 404 by the global handler. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
