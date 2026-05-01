package com.omnieats.identity_service.service;

import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
        log.debug("Registering new user: email={}", normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.error("Registration failed — email already in use: {}", normalizedEmail);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already in use.");
        }

        User user = new User(
                normalizedEmail,
                name.trim(),
                passwordEncoder.encode(rawPassword),
                List.of("USER")
        );

        User saved = userRepository.save(user);
        log.info("User registered: id={}, email={}", saved.getId(), normalizedEmail);
        return saved;
    }

    /**
     * Validates credentials and returns a signed JWT.
     *
     * @return JWT string
     * @throws ResponseStatusException 401 if credentials are invalid
     */
    public String login(String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        log.debug("Login attempt: email={}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.error("Login failed — unknown email: {}", normalizedEmail);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            log.error("Login failed — wrong password for email: {}", normalizedEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("User logged in: id={}, email={}", user.getId(), normalizedEmail);
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
