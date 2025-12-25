package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ✅ FIX: Added underscore (_) after Library.
    // This correctly maps to: Review -> Library -> Id
    List<Review> findByLibrary_IdAndIsVisibleTrueOrderByCreatedAtDesc(Long libraryId);

    // For Admin: Get ALL reviews for a specific library (Visible or Hidden)
    List<Review> findByLibrary_IdOrderByCreatedAtDesc(Long libraryId);

    // Check duplicate
    boolean existsByBookingId(Long bookingId);

    // Admin global list
    List<Review> findAllByOrderByCreatedAtDesc();
}