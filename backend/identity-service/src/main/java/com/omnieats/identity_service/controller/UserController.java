package com.omnieats.identity_service.controller;

import com.omnieats.identity_service.controller.dto.UpdateProfileRequest;
import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public User getCurrentUser(@RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = UUID.fromString(userIdHeader);
        return userService.getUserById(userId);
    }

    @PutMapping("/me")
    public User updateProfile(@RequestHeader("X-User-Id") String userIdHeader,
                              @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(userIdHeader);
        return userService.updateProfile(userId, request);
    }
}
