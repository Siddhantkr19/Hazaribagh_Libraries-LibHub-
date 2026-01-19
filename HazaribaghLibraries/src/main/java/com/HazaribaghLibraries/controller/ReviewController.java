package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ApiResponse; // ✅ Import
import com.HazaribaghLibraries.dto.ReviewRequestDTO;
import com.HazaribaghLibraries.entity.Review;
import com.HazaribaghLibraries.service.ReviewService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Ratings and Feedback System")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 1. Check Eligibility
    @GetMapping("/check-eligibility")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkEligibility(@RequestParam Long bookingId) {
        // Global Handler catches errors automatically
        return ResponseEntity.ok(new ApiResponse<>("Eligibility status", reviewService.checkEligibility(bookingId)));
    }

    // 2. Submit Review
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<String>> submitReview(@RequestBody ReviewRequestDTO request) {
        reviewService.submitReview(request);
        return ResponseEntity.ok(new ApiResponse<>("Review submitted successfully!"));
    }

    // 3. Get Public Reviews
    @GetMapping("/library/{libraryId}")
    public ResponseEntity<ApiResponse<List<Review>>> getLibraryReviews(@PathVariable Long libraryId) {
        return ResponseEntity.ok(new ApiResponse<>("Reviews fetched", reviewService.getPublicLibraryReviews(libraryId)));
    }

    // 4. Admin: Get All Reviews
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<Review>>> getAllReviewsForAdmin() {
        return ResponseEntity.ok(new ApiResponse<>("All reviews fetched", reviewService.getAllReviewsForAdmin()));
    }

    // 5. Admin: Toggle Visibility
    @PutMapping("/admin/toggle-visibility/{id}")
    public ResponseEntity<ApiResponse<Review>> toggleVisibility(@PathVariable Long id) {
        Review updatedReview = reviewService.toggleVisibilityAndReturn(id);
        return ResponseEntity.ok(new ApiResponse<>("Visibility toggled", updatedReview));
    }
}