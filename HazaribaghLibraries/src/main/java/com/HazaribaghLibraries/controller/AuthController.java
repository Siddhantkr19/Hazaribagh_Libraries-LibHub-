package com.HazaribaghLibraries.controller;
import com.HazaribaghLibraries.dto.LoginRequestDTO;
import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.HazaribaghLibraries.dto.SignupRequestDTO;
@RestController
@RequestMapping("/api/auth") // This groups all auth URLs under /api/auth
@CrossOrigin(origins = "http://localhost:5173") // Allows your React frontend to connect
public class AuthController {

    private final AuthService authService;

    // Injection: Connects the Service to this Controller
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Endpoint: POST http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequestDTO loginRequest) {

        // 1. Call the service to check password
        UserDTO userDTO = authService.login(loginRequest);

        // 2. If successful, return the User Profile (200 OK)
        return ResponseEntity.ok(userDTO);
    }


    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody SignupRequestDTO signupRequest) {
        return ResponseEntity.ok(authService.registerUser(signupRequest));
    }
}
