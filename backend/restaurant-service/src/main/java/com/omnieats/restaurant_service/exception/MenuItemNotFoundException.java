package com.omnieats.restaurant_service.exception;

public class MenuItemNotFoundException extends ResourceNotFoundException {
    public MenuItemNotFoundException(String message) {
        super(message);
    }
}
