package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ApiResponse; // ✅ Import
import com.HazaribaghLibraries.service.PhotoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Utility", description = "Health Checks and File Uploads")
public class FileUploadController {

    private final PhotoService photoService;

    public FileUploadController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "File is empty"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Only JPG, PNG, or WebP images are allowed"));
            }

            // Upload
            String fileUrl = photoService.uploadImage(file);
            return ResponseEntity.ok(new ApiResponse<>("File uploaded successfully", fileUrl));

        } catch (IOException e) {
            // Throwing RuntimeException allows GlobalExceptionHandler to catch it properly
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}