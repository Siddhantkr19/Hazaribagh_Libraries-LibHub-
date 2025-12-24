package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. Get all reviews for a specific library (for Public Page)
    List<Review> findByLibraryIdAndIsVisibleTrueOrderByCreatedAtDesc(Long libraryId);

    // 2. Get ALL reviews for a library (for Admin)
    List<Review> findByLibraryIdOrderByCreatedAtDesc(Long libraryId);

    // 3. Check if a review already exists for this booking ID
    boolean existsByBookingId(Long bookingId);
}