package com.HazaribaghLibraries.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import java.util.List;
import java.util.ArrayList; // Import ArrayList to initialize lists

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "libraries")
@BatchSize(size = 20)
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    private String locationTag;

    private String description;

    private Double originalPrice;

    private Double offerPrice;

    private String openingHours;

    @Column(nullable = false)
    private Integer totalSeats;

    @Column(nullable = false)
    private String contactNumber;

    // --- ⚠️ CHANGED SECTION START ---

    // Old @ElementCollection is replaced with @OneToMany
    // CascadeType.ALL means if you save a Library, it auto-saves the amenities
    // orphanRemoval = true means if you remove an amenity from the list, it deletes from DB
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "library_id") // This creates the Foreign Key in the child table
    @BatchSize(size = 20)
    private List<LibraryAmenity> amenities = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "library_id")
    @BatchSize(size = 20)
    private List<LibraryImage> images = new ArrayList<>();

    // --- ⚠️ CHANGED SECTION END ---

    private Double averageRating = 0.0;
    private Integer totalReviews = 0;

    // 👇 ADD THIS LIST AT THE BOTTOM
    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    @JsonIgnore // 🛑 Critical: Stops the infinite loop
    private List<Booking> bookings;
}