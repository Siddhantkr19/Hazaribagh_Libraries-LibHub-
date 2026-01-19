package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ApiResponse; // ✅ Import
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Utility")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<ApiResponse<String>> checkHealth() {
        return ResponseEntity.ok(new ApiResponse<>("Server is running"));
    }
}