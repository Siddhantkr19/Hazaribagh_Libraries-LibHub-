package com.HazaribaghLibraries.controller;

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
    public ResponseEntity<String> checkHealth() {
        System.out.println("Health check API was hit at " + java.time.LocalDateTime.now());
        return ResponseEntity.ok("Server is running");
    }
}