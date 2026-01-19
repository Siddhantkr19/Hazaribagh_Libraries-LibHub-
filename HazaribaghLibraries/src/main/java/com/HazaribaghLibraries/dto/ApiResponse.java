package com.HazaribaghLibraries.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Constructor for Success (Data + Message)
    public ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for Success (Message Only)
    public ApiResponse(String message) {
        this.success = true;
        this.message = message;
        this.data = null;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor for Error
    public ApiResponse(boolean success, String message) {
        this.success = success; // should be false
        this.message = message;
        this.data = null;
        this.timestamp = LocalDateTime.now();
    }
}