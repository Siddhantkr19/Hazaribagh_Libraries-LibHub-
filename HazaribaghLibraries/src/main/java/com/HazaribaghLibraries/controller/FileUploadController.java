package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/public")
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class FileUploadController {

    private final PhotoService photoService;

    // Inject the Cloudinary PhotoService
    public FileUploadController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            // 1. Validate File Type (Images only)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only JPG/PNG images are allowed");
            }

            // 2. Upload to Cloudinary (Permanent Storage) ✅
            String fileUrl = photoService.uploadImage(file);

            // 3. Return the Cloudinary URL
            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}