package com.HazaribaghLibraries.controller;


import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.service.PhotoService;
import com.HazaribaghLibraries.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;

@RequestMapping("/api/auth") // Common standard for login/register APIs
//@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Authentication")
@RestController
public class UserController {
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;

    }
    // URL: POST http://localhost:8080/api/auth/register

//    // this not working
//    @PostMapping("/registers")
//     public ResponseEntity<UserDTO> registerUser(@RequestBody User user){
//        return ResponseEntity.ok(userService.registerUser(user));
//
//    }

    // cheak login
    // URL: GET http://localhost:8080/api/auth/profile?userEmail=rahul@gmail.com
    @GetMapping("/profile")
public  ResponseEntity<UserDTO> getUserProfile(@RequestParam String userEmail){
    return ResponseEntity.ok(userService.getUserProfile(userEmail));
}
    // URL: PUT http://localhost:8080/api/auth/upload-photo
    @PutMapping("/upload-photo")
    public ResponseEntity<?> uploadProfilePhoto(
            @RequestParam("userEmail") String userEmail,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only Image files (JPG, PNG) are allowed");
            }

            // Calls UserService -> which calls PhotoService -> which uploads to Cloud
            return ResponseEntity.ok(userService.uploadProfilePicture(userEmail, file));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error uploading file: " + e.getMessage());
        }
    }


}
