package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.dto.ReviewRequestDTO;
import com.HazaribaghLibraries.entity.*;
import com.HazaribaghLibraries.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private LibraryRepository libraryRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;

    // 1. Check Eligibility
    public Map<String, Object> checkEligibility(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // For testing: plusDays(0). For Prod: plusDays(3)
        boolean isTimePassed = booking.getBookingDate().plusDays(0).isBefore(LocalDateTime.now());
        boolean alreadyReviewed = reviewRepository.existsByBookingId(bookingId);

        if (!isTimePassed) {
            return Map.of("canReview", false, "reason", "You can review after 3 days.");
        }
        if (alreadyReviewed) {
            return Map.of("canReview", false, "reason", "You have already reviewed this booking.");
        }
        return Map.of("canReview", true);
    }

    // 2. Submit Review
    @Transactional
    public void submitReview(ReviewRequestDTO request) {
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Library library = booking.getLibrary();

        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new RuntimeException("Review already exists.");
        }

        Review review = new Review();
        review.setUser(user);
        review.setBooking(booking);
        review.setLibrary(library);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setVisible(true);

        reviewRepository.save(review);

        // ✅ ONLY use recalculate. It effectively updates the rating safely.
        recalculateLibraryRating(library);
    }

    // 3. Get Public Reviews + SELF-HEAL RATING
    public List<Review> getPublicLibraryReviews(Long libraryId) {
        // 1. Fetch all visible reviews
        List<Review> reviews = reviewRepository.findByLibrary_IdAndIsVisibleTrueOrderByCreatedAtDesc(libraryId);

        // 2. SELF-HEALING LOGIC
        // If the database numbers don't match the actual reviews, fix it instantly.
        if (!reviews.isEmpty()) {
            double sum = 0;
            for (Review r : reviews) {
                sum += r.getRating();
            }
            double actualAverage = sum / reviews.size();
            int actualCount = reviews.size();

            Library library = libraryRepository.findById(libraryId).orElse(null);

            if (library != null) {
                boolean isOutOfSync = library.getAverageRating() == null ||
                        Math.abs(library.getAverageRating() - actualAverage) > 0.01 ||
                        library.getTotalReviews() != actualCount;

                if (isOutOfSync) {
                    log.warn("🔄 Self-Healing triggered for Library ID {}", libraryId);
                    library.setAverageRating(actualAverage);
                    library.setTotalReviews(actualCount);
                    libraryRepository.save(library);
                }
            }
        }

        return reviews;
    }

    // 4. Admin: Get All Reviews
    public List<Review> getAllReviewsForAdmin() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    // 5. Admin: Toggle Visibility
    @Transactional
    public Review toggleVisibilityAndReturn(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        review.setVisible(!review.isVisible());
        reviewRepository.save(review);

        // ✅ IMPORTANT: Update rating when a review is hidden/shown
        recalculateLibraryRating(review.getLibrary());

        return review;
    }

    // --- Helper Method ---
    private void recalculateLibraryRating(Library library) {
        List<Review> reviews = reviewRepository.findByLibrary_IdAndIsVisibleTrueOrderByCreatedAtDesc(library.getId());

        if (reviews.isEmpty()) {
            library.setAverageRating(0.0);
            library.setTotalReviews(0);
        } else {
            double average = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            library.setAverageRating(average);
            library.setTotalReviews(reviews.size());
        }
        libraryRepository.save(library);
    }
}