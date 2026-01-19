package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ApiResponse; // ✅ Import
import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/api/auth")
@Tag(name = "Authentication")
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 1. Get Profile
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getUserProfile(@RequestParam String userEmail) {
        return ResponseEntity.ok(new ApiResponse<>("Profile fetched", userService.getUserProfile(userEmail)));
    }

    // 2. Upload Profile Photo
    @PutMapping("/upload-photo")
    public ResponseEntity<ApiResponse<UserDTO>> uploadProfilePhoto(
            @RequestParam("userEmail") String userEmail,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Please select a file"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Only Image files (JPG, PNG) are allowed"));
            }

            // Calls service to upload & save
            UserDTO updatedUser = userService.uploadProfilePicture(userEmail, file);
            return ResponseEntity.ok(new ApiResponse<>("Profile photo updated", updatedUser));

        } catch (IOException e) {
            throw new RuntimeException("Error uploading file: " + e.getMessage());
        }
    }
}
