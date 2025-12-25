package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ReviewRequestDTO;
import com.HazaribaghLibraries.entity.Review;
import com.HazaribaghLibraries.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
//@CrossOrigin("http://localhost:5173")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 1. CHECK ELIGIBILITY
    @GetMapping("/check-eligibility")
    public ResponseEntity<Map<String, Object>> checkEligibility(@RequestParam Long bookingId) {
        try {
            return ResponseEntity.ok(reviewService.checkEligibility(bookingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. SUBMIT REVIEW
    @PostMapping("/submit")
    public ResponseEntity<?> submitReview(@RequestBody ReviewRequestDTO request) {
        try {
            reviewService.submitReview(request);
            return ResponseEntity.ok("Review submitted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error submitting review.");
        }
    }

    // 3. GET PUBLIC REVIEWS
    @GetMapping("/library/{libraryId}")
    public ResponseEntity<List<Review>> getLibraryReviews(@PathVariable Long libraryId) {
        return ResponseEntity.ok(reviewService.getPublicLibraryReviews(libraryId));
    }

    // 4. ADMIN: GET ALL REVIEWS
    @GetMapping("/admin/all")
    public ResponseEntity<List<Review>> getAllReviewsForAdmin() {
        return ResponseEntity.ok(reviewService.getAllReviewsForAdmin());
    }

    // 3. Admin: Toggle Visibility (Returns Updated Object)
    @PutMapping("/admin/toggle-visibility/{id}")
    public ResponseEntity<Review> toggleVisibility(@PathVariable Long id) {
        try {
            Review updatedReview = reviewService.toggleVisibilityAndReturn(id);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}