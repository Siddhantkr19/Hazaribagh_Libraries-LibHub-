package com.HazaribaghLibraries.config;

import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${library.app.adminEmail}")
    private String adminEmail;

    @Value("${library.app.adminPassword}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setName("Siddhant Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword)); // Encrypted for security
            admin.setPhoneNumber("6201179442"); // Placeholder
            admin.setRole(User.Role.Admin); // ✅ Essential for Admin access
            userRepository.save(admin);
            log.info("✅ Default Admin User Created.");
        }
    }
}