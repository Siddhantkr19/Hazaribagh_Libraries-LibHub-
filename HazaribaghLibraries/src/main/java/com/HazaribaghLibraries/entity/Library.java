package com.HazaribaghLibraries.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "libraries")
@BatchSize(size = 20)

public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long  id ;

    @Column(nullable = false)
    private String name ;

    @Column(nullable = false , length = 500)
    private String address ;

    private String locationTag; // e.g., "Matwari" (For easier searching)

    private String description ;

    private Double originalPrice;  // eg  :  400

    private Double offerPrice ;  // eg : offer upto 20%  which is approx 350 .

    private String openingHours;

    @Column(nullable = false)
    private  Integer totalSeats ;   // eg : 60

    @Column(nullable = false)
    private String contactNumber ;

    @ElementCollection
    @CollectionTable(name = "library_amenities", joinColumns = @JoinColumn(name = "library_id"))
    @Column(name = "amenity")
    @BatchSize(size = 10) // Fetches amenities for 10 libraries in 1 go
    private List<String> amenities; // e.g., ["AC", "WiFi", "RO Water"]

    // This creates a separate table for image URLs
    @ElementCollection
    @CollectionTable(name = "library_images", joinColumns = @JoinColumn(name = "library_id"))
    @Column(name = "image_url")
    @BatchSize(size = 10) // Fetches images for 10 libraries in 1 go
    private List<String> images;

    // ✅ NEW: Feedback Statistics
    private Double averageRating = 0.0; // Default 0.0
    private Integer totalReviews = 0;   // Default 0
}
