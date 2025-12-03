package com.HazaribaghLibraries.controller;


import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RequestMapping("/api/auth") // Common standard for login/register APIs
@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    // URL: POST http://localhost:8080/api/auth/register

    @PostMapping("/register")
     public ResponseEntity<UserDTO> registerUser(@RequestBody User user){
        return ResponseEntity.ok(userService.registerUser(user));

    }
    // URL: GET http://localhost:8080/api/auth/profile?userEmail=rahul@gmail.com
    @GetMapping("/profile")
public  ResponseEntity<UserDTO> getUserProfile(@RequestParam String userEmail){
    return ResponseEntity.ok(userService.getUserProfile(userEmail));
}


}
