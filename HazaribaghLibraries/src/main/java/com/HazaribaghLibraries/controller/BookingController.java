package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.*;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.service.BookingService;
import com.cloudinary.Api;
import com.razorpay.RazorpayException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import com.HazaribaghLibraries.service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Seat Reservation, Payment & Dashboard")
//@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {
    private  final BookingService bookingService;
    private final EmailService emailService;

    public BookingController(BookingService bookingService, EmailService emailService) {
        this.bookingService = bookingService;
        this.emailService = emailService;
    }


     // 1. STEP ONE: Create Order

     @PostMapping("/create-order")
     public ResponseEntity<ApiResponse<Booking>> createOrder(@RequestParam String userEmail, @RequestBody BookingRequestDTO request) throws RazorpayException {
         // Global Handler catches RazorpayException automatically
         Booking booking = bookingService.createOrder(userEmail, request.getLibraryId());
         return ResponseEntity.ok(new ApiResponse<>("Order created", booking));
     }

    // 2. Verify Payment
    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<Booking>> verifyPayment(@RequestBody PaymentVerificationDTO verificationDTO) {
        Booking confirmedBooking = bookingService.verifyPayment(verificationDTO);

        // Email Logic (Keep this try-catch as it's a non-blocking background task)
        try {
            String libName = (confirmedBooking.getLibrary() != null) ? confirmedBooking.getLibrary().getName() : "Library Seat";
            String userEmail = (confirmedBooking.getUser() != null) ? confirmedBooking.getUser().getEmail() : "unknown@error.com";

            emailService.sendBookingConfirmation(userEmail, libName, confirmedBooking.getAmountPaid(), confirmedBooking.getId());
        } catch (Exception e) {
            log.error("Failed to send confirmation email: {}", e.getMessage());
        }

        return ResponseEntity.ok(new ApiResponse<>("Payment verified & Booking confirmed!", confirmedBooking));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<DashboardBookingDTO>>> getUserDashboard(@RequestParam String userEmail) {
        List<DashboardBookingDTO> data = bookingService.getUserDashboard(userEmail);
        return ResponseEntity.ok(new ApiResponse<>("Dashboard data fetched", data));

    }
    // 3. Get Payment History
    // URL: GET http://localhost:8080/api/bookings/history?userEmail=rahul@gmail.com&libraryId=1
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PaymentHistoryDTO>>> getHistory(
            @RequestParam String userEmail,
            @RequestParam Long libraryId) {

        List<PaymentHistoryDTO> history = bookingService.getPaymentHistory(userEmail, libraryId);
        return ResponseEntity.ok(new ApiResponse<>("History fetched", history));
    }



    @GetMapping("/check-eligibility")
    public ResponseEntity<ApiResponse<Boolean>> checkWelcomeOfferEligibility(@RequestParam String userEmail) {
        boolean isNew = bookingService.isNewUser(userEmail);
        return ResponseEntity.ok(new ApiResponse<>("Eligibility checked", isNew));
    }
}
