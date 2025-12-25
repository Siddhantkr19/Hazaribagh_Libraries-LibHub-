package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.LoginRequestDTO;
import com.HazaribaghLibraries.dto.SignupRequestDTO;
import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.repository.UserRepository;
import com.HazaribaghLibraries.security.jwt.JwtUtils;
import com.HazaribaghLibraries.service.AuthService;
import com.HazaribaghLibraries.security.services.UserDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Must set allowCredentials
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }


    // 1. Check Email (Public)
    // URL: GET /api/auth/check-email?email=abc@gmail.com
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        // 1. Perform Login (Authentication)
        UserDTO userDTO = authService.login(loginRequest);

        // 2. [NEW] Generate JWT Cookie
        UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getEmail());
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // 3. Return User Data + Set Cookie in Header
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(userDTO);
    }

    @PostMapping("/register")
    // Change return type to ResponseEntity<?> to allow returning generic Maps for errors
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequestDTO signupRequest) {
        try {
            // Try to register the user
            UserDTO registeredUser = authService.registerUser(signupRequest);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            // Catch the "Email already exists!" exception from AuthService
            // Return a 400 Bad Request with a JSON object: { "message": "Email already exists!" }
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }
    // [NEW] Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("You've been signed out!");
    }

    // [NEW] 1. Request Password Reset (User sends Email)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.processForgotPassword(email);
            return ResponseEntity.ok("Password reset link sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // [NEW] 2. Verify Token (Frontend checks if link is valid)
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        boolean isValid = authService.verifyResetToken(token);
        if (isValid) {
            return ResponseEntity.ok("Token is valid");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    // [NEW] 3. Submit New Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok("Password successfully changed! Please login.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        // If not authenticated, the Security Filter usually blocks this before it reaches here.
        // But for safety:
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // The JWT Filter has already put the email in the Authentication object
        String email = authentication.getName();

        // Fetch User and Map to DTO
        // (Assuming you have access to UserRepository here, or use AuthService)
        // Ideally, put this logic in AuthService, but for now we can do it here:

        UserDTO userDTO = authService.getUserByEmail(email); // We need to add this method to AuthService
        return ResponseEntity.ok(userDTO);
    }
}