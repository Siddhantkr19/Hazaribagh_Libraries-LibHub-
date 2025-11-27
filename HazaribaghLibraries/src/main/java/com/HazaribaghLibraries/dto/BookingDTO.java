package com.HazaribaghLibraries.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDTO {
    // Booking Identity
    private Long bookingId;
    private String status; // CONFIRMED or EXPIRED
    private Double amountPaid; // e.g. 350.0

    // Seat Info
    private String seatNumber; // e.g. "B4"

    // Library Info (So they know where to go)
    private String libraryName;
    private String libraryAddress;
    private String libraryOwnerContact;

    // Time Calculations
    private LocalDateTime bookingDate;
    private LocalDateTime validUntil; // Expiry Date

    // The "Red Alert" Counter
    private long daysRemaining; // e.g. 29 (Calculated in Service)
}
