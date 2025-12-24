package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.dto.ReviewRequestDTO;
import com.HazaribaghLibraries.entity.*;
import com.HazaribaghLibraries.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private LibraryRepository libraryRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;

    // 1. Logic: Check Eligibility
    public Map<String, Object> checkEligibility(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Rule: Check if 3 days have passed (Currently set to 0 for testing)
        boolean isTimePassed = booking.getBookingDate().plusDays(0).isBefore(LocalDateTime.now());
        boolean alreadyReviewed = reviewRepository.existsByBookingId(bookingId);

        if (!isTimePassed) {
            return Map.of("canReview", false, "reason", "You can review after 3 days of booking.");
        }
        if (alreadyReviewed) {
            return Map.of("canReview", false, "reason", "You have already reviewed this booking.");
        }
        return Map.of("canReview", true);
    }

    // 2. Logic: Submit Review
    public void submitReview(ReviewRequestDTO request) {

        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // ✅ derive library from booking
        Library library = booking.getLibrary();

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("Review already exists for this booking.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setBooking(booking);
        review.setLibrary(library);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        reviewRepository.save(review);

        updateLibraryRating(library, request.getRating());
    }

    // 3. Logic: Get Public Reviews
    public List<Review> getPublicLibraryReviews(Long libraryId) {
        return reviewRepository.findByLibraryIdAndIsVisibleTrueOrderByCreatedAtDesc(libraryId);
    }

    // 4. Logic: Get All Reviews (Admin)
    public List<Review> getAllReviewsForAdmin() {
        return reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // 5. Logic: Toggle Visibility (Admin)
// In ReviewService
    @Transactional
    public Review toggleVisibilityAndReturn(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));

        // Flip the visibility status
        review.setVisible(!review.isVisible());

        // Save and return the updated review
        return reviewRepository.save(review);
    }

    // --- Helper Method ---
    private void updateLibraryRating(Library library, int newRating) {

        // ✅ Handle first review case
        Double currentAvg = library.getAverageRating();
        Integer totalReviews = library.getTotalReviews();

        if (currentAvg == null || totalReviews == null) {
            library.setAverageRating((double) newRating);
            library.setTotalReviews(1);
        } else {
            double updatedAvg =
                    ((currentAvg * totalReviews) + newRating) / (totalReviews + 1);

            library.setAverageRating(updatedAvg);
            library.setTotalReviews(totalReviews + 1);
        }

        libraryRepository.save(library);
    }

}