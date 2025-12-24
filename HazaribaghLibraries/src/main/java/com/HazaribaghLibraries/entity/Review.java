package com.HazaribaghLibraries.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which student wrote it?
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Which library is it for?
    @ManyToOne
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    // ✅ CRITICAL: Links review to a specific transaction
    // Enforces "One Review Per Booking"
    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    private Integer rating; // 1 to 5

    @Column(length = 1000)
    private String comment; // "AC was good..."

    private boolean isVisible = true; // Admin can hide abusive reviews

    private LocalDateTime createdAt = LocalDateTime.now();
}