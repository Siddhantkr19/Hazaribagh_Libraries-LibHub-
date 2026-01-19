package com.HazaribaghLibraries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
    private int status;        // e.g., 400, 404, 500
    private String message;    // e.g., "User not found"
    private LocalDateTime timestamp;
}