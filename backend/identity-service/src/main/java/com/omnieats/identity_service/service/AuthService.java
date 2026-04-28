package com.omnieats.identity_service.service;

import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user.
     *
     * @return the persisted {@link User}
     * @throws ResponseStatusException 409 if the email is already in use
     */
    public User register(String name, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already in use.");
        }

        User user = new User(
                normalizedEmail,
                name.trim(),
                passwordEncoder.encode(rawPassword),
                List.of("USER")
        );

        return userRepository.save(user);
    }

    /**
     * Validates credentials and returns a signed JWT.
     *
     * @return JWT string
     * @throws ResponseStatusException 401 if credentials are invalid
     */
    public String login(String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return jwtService.issueToken(user);
    }

    /**
     * Loads a user by email — used by the controller to build the response DTO
     * after a successful {@link #login} call.
     *
     * @throws ResponseStatusException 404 if the user does not exist
     */
    public User loadByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
