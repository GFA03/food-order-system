package com.omnieats.identity_service.service;

import com.omnieats.identity_service.controller.dto.UpdateProfileRequest;
import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.model.UserProfile;
import com.omnieats.identity_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(UUID userId) {
        log.debug("Fetching user: id={}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: id={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    @Transactional
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        log.debug("Updating profile: userId={}", userId);
        User user = getUserById(userId);

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile(user);
            user.setProfile(profile);
        }

        if (request.getDeliveryAddress() != null) {
            profile.setDeliveryAddress(request.getDeliveryAddress());
        }
        if (request.getLatitude() != null) {
            profile.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            profile.setLongitude(request.getLongitude());
        }
        if (request.getDietaryPreferences() != null) {
            profile.getDietaryPreferences().clear();
            profile.getDietaryPreferences().addAll(request.getDietaryPreferences());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated: userId={}", userId);
        return saved;
    }
}
