package com.HazaribaghLibraries.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELATIONSHIP: Many bookings can belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // RELATIONSHIP: Many bookings can belong to One Library, means user can book multiple booking in one library
    @ManyToOne
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    private String seatNumber; // e.g., "B4" or "Ground-12" or 21 seat number

    private LocalDateTime bookingDate; // Timestamp of when they clicked "Pay"
    private LocalDateTime validUntil; // Expiry Date (30 days later)

    private Double amountPaid; // e.g., 350.0

    private String paymentId; // The Transaction ID from Razorpay/UPI

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public enum BookingStatus {
        PENDING,   // User clicked book, but hasn't paid yet
        CONFIRMED, // Payment success, seat booked
        EXPIRED,   // 30 days are over
        CANCELLED  // Refunded
    }
}

