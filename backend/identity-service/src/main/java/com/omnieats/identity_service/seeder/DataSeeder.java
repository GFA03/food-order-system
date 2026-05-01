package com.omnieats.identity_service.seeder;

import com.omnieats.identity_service.model.User;
import com.omnieats.identity_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@omnieats.com")) {
            logger.info("Seeding initial user data...");

            User admin = new User(
                    "admin@omnieats.com",
                    "Admin User",
                    passwordEncoder.encode("admin123"),
                    List.of("USER", "ADMIN")
            );
            userRepository.save(admin);
            
            User regularUser = new User(
                    "user@omnieats.com",
                    "Regular User",
                    passwordEncoder.encode("user123"),
                    List.of("USER")
            );
            userRepository.save(regularUser);

            logger.info("Database seeding completed. Admin and Regular user created.");
        } else {
            logger.info("Users already exist, skipping seeding.");
        }
    }
}
