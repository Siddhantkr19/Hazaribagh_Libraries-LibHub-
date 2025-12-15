package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.dto.LoginRequestDTO;
import com.HazaribaghLibraries.dto.SignupRequestDTO;
import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.HazaribaghLibraries.service.EmailService; // [NEW]
import java.time.LocalDateTime;
import java.util.UUID;
@Service
public class AuthService {
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    // [NEW] Inject these beans
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthService(ModelMapper modelMapper, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    // [CHANGED] Login now performs authentication via Manager
    // Returns Entity (to get data) but Authentication is handled separately in Controller
    public UserDTO login(LoginRequestDTO loginRequestDTO) {
        // 1. Authenticate using Spring Security (This checks email & password automatically)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));

        // 2. Set Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Get User Details to return to Frontend
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return modelMapper.map(user, UserDTO.class);
    }

    // [CHANGED] Register now encodes password
    public UserDTO registerUser(SignupRequestDTO signupRequest) {
        if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        User user = modelMapper.map(signupRequest, User.class);

        // [NEW] Encode Password before saving!
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        if (user.getRole() == null) {
            user.setRole(User.Role.Student);
        }

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Generate Token
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15)); // Valid for 15 mins

        userRepository.save(user);

        // Send Email
        emailService.sendResetEmail(user.getEmail(), token);
    }

    // [NEW] 2. Verify Token (Optional, for UI check)
    public boolean verifyResetToken(String token) {
        return userRepository.findByResetToken(token)
                .map(user -> user.getResetTokenExpiry().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    // [NEW] 3. Reset Password
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        // Update Password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear Token
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }


    // ... inside AuthService class ...

    // [NEW] Helper to get UserDTO by Email
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UserDTO.class);
    }
}