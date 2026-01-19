package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ApiResponse;
import com.HazaribaghLibraries.dto.LoginRequestDTO;
import com.HazaribaghLibraries.dto.SignupRequestDTO;
import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.repository.UserRepository;
import com.HazaribaghLibraries.security.jwt.JwtUtils;
import com.HazaribaghLibraries.service.AuthService;
import com.HazaribaghLibraries.security.services.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "Login, Register, and Password Reset")
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
    public ResponseEntity<ApiResponse<Boolean>>checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        // ✅ Return wrapped response
        return ResponseEntity.ok(new ApiResponse<>("Email check successful", exists));
    }
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserDTO>> login(@RequestBody LoginRequestDTO loginRequest) {
        // 1. Perform Login (Authentication)
        UserDTO userDTO = authService.login(loginRequest);

        // 2. [NEW] Generate JWT Cookie
        UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getEmail());
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // 3. Return User Data + Set Cookie in Header
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new ApiResponse<>("Login successful", userDTO));
    }

    @PostMapping("/register")
    // Change return type to ResponseEntity<?> to allow returning generic Maps for errors
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody SignupRequestDTO signupRequest) {


            UserDTO registeredUser = authService.registerUser(signupRequest);
            return ResponseEntity.ok(new ApiResponse<>("User registered successfully", registeredUser));

    }
    // [NEW] Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new ApiResponse<>("You've been signed out!"));
    }

    // [NEW] 1. Request Password Reset (User sends Email)
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            authService.processForgotPassword(email);
            return ResponseEntity.ok( new ApiResponse<>("Password reset link sent to your email."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage()));
        }
    }

    // [NEW] 2. Verify Token (Frontend checks if link is valid)
    @GetMapping("/verify-token")
    public ResponseEntity<ApiResponse<String>> verifyToken(@RequestParam String token) {
        boolean isValid = authService.verifyResetToken(token);
        if (isValid) {
            return ResponseEntity.ok(new ApiResponse<>("Token is valid"));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse<>("Invalid or expired token"));
        }
    }

    // [NEW] 3. Submit New Password
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        try {
            authService.resetPassword(token, newPassword);
            return ResponseEntity.ok (new ApiResponse<>("Password successfully changed! Please login."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage()));
        }
    }
    @GetMapping("/current-user")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
        // 1. Safety Check
        if (authentication == null || !authentication.isAuthenticated()) {
            // ✅ Return a proper JSON error instead of an empty 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "User not authenticated"));
        }

        // 2. Fetch Data
        String email = authentication.getName();
        UserDTO userDTO = authService.getUserByEmail(email);

        // 3. Return Wrapped Response (Message + Data)
        // ✅ Fixed Constructor: Added the message string first
        return ResponseEntity.ok(new ApiResponse<>("User profile fetched", userDTO));
    }
}