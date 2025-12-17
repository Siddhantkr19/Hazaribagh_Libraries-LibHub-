package com.HazaribaghLibraries.dto;

import lombok.Data;

@Data
public class OfflineBookingRequest {
    private String studentEmail; // We use email to find the student
    private Long libraryId;
    private String seatNumber;   // Optional (can be assigned later)
    private Double amountPaid;   // Admin enters how much cash they took
    private int durationDays;    // e.g., 30 days
}