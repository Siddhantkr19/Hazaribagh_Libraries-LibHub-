package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.LoginRequestDTO;
import com.HazaribaghLibraries.dto.SignupRequestDTO;
import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.security.jwt.JwtUtils;
import com.HazaribaghLibraries.service.AuthService;
import com.HazaribaghLibraries.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Must set allowCredentials
public class AuthController {

    private final AuthService authService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
    public ResponseEntity<UserDTO> register(@RequestBody SignupRequestDTO signupRequest) {
        return ResponseEntity.ok(authService.registerUser(signupRequest));
    }

    // [NEW] Logout Endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body("You've been signed out!");
    }
}