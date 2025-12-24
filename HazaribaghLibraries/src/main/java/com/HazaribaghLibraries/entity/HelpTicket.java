package com.HazaribaghLibraries.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "help_tickets") // ✅ This creates a physical table in your database
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelpTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Stores who sent the message
    @Column(nullable = false)
    private String userEmail;

    // Stores the type of issue (Refund, Complaint, etc.)
    @Column(nullable = false)
    private String subject;

    // Stores the full message content (Large text allowed)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    // Optional: Links to a specific booking ID if relevant
    private Long bookingId;

    // Auto-saves the exact time the message was sent
    private LocalDateTime createdAt = LocalDateTime.now();

    // Tracks if Admin has seen/solved it ("PENDING", "RESOLVED")
    private String status = "PENDING";
}