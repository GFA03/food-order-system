package com.omnieats.restaurant_service.exception;

public class CuisineTagNotFoundException extends ResourceNotFoundException {
    public CuisineTagNotFoundException(String message) {
        super(message);
    }
}
