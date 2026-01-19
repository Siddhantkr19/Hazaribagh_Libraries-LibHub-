package com.HazaribaghLibraries.config;

import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("siddhantkumar7488@gmail.com")) {
            User admin = new User();
            admin.setName("Siddhant Admin");
            admin.setEmail("siddhantkumar7488@gmail.com");
            admin.setPassword(passwordEncoder.encode("sidd@1234")); // Encrypted for security
            admin.setPhoneNumber("0000000000"); // Placeholder
            admin.setRole(User.Role.Admin); // ✅ Essential for Admin access
            userRepository.save(admin);
            log.info("✅ Default Admin User Created.");
        }
    }
}