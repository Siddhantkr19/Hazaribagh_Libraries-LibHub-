package com.HazaribaghLibraries.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfflineBookingRequest {


    @NotBlank(message = "Student email is required")
    @Email(message = "Invalid email format")
    private String studentEmail; // We use email to find the student

    @NotNull(message = "Library ID is required")
    private Long libraryId;
    private String seatNumber;   // Optional (can be assigned later)

    @NotNull(message = "Amount paid is required")
    @Min(value = 0, message = "Amount cannot be negative")
    private Double amountPaid;   // Admin enters how much cash they took

    @Min(value = 1, message = "Duration must be at least 1 day")

    private int durationDays;    // e.g., 30 days
}