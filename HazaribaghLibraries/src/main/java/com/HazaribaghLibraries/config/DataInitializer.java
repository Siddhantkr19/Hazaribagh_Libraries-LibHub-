package com.HazaribaghLibraries.config;

import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
        // Check if the admin already exists
        if (!userRepository.existsByEmail("siddhantkumar7488@gmail.com")) {

            User admin = new User();
            admin.setName("Siddhant Admin");
            admin.setEmail("siddhantkumar7488@gmail.com");
            // We MUST hash the password using BCrypt
            admin.setPassword(passwordEncoder.encode("sidd@1234"));
            admin.setPhoneNumber("9999999999"); // Dummy number to satisfy validation
            admin.setRole(User.Role.Admin); // ✅ Sets you as ADMIN

            userRepository.save(admin);

            System.out.println("✅ ADMIN USER CREATED: siddhantkumar7488@gmail.com");
        } else {
            System.out.println("ℹ️ Admin user already exists.");
        }
    }
}