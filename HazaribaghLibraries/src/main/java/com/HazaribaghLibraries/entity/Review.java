package com.HazaribaghLibraries.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "library_id", nullable = false)
    private Library library;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    private Integer rating;

    @Column(length = 1000)
    private String comment;

    // ✅ Ensures frontend gets "isVisible": true instead of "visible": true
    @JsonProperty("isVisible")
    private boolean isVisible = true;

    private LocalDateTime createdAt = LocalDateTime.now();
}