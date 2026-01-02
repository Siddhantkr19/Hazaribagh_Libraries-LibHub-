package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.BookingRequestDTO;
import com.HazaribaghLibraries.dto.DashboardBookingDTO;
import com.HazaribaghLibraries.dto.PaymentHistoryDTO;
import com.HazaribaghLibraries.dto.PaymentVerificationDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.service.BookingService;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import com.HazaribaghLibraries.service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
//@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {
    private  final BookingService bookingService;
    private final EmailService emailService;

    public BookingController(BookingService bookingService, EmailService emailService) {
        this.bookingService = bookingService;
        this.emailService = emailService;
    }


     // 1. STEP ONE: Create Order
     // Frontend calls this when user clicks "Pay"
     @PostMapping("/create-order")
     public ResponseEntity<Booking> createOrder(@RequestParam String userEmail, @RequestBody BookingRequestDTO request) {
         try {
             Booking booking = bookingService.createOrder(userEmail, request.getLibraryId());
             return ResponseEntity.ok(booking);
         } catch (RazorpayException e) {
             return ResponseEntity.internalServerError().build();
         }
     }

    // 2. STEP TWO: Verify Payment
    // 2. STEP TWO: Verify Payment
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDTO verificationDTO) {
        try {
            // 1. Verify Payment & Save to DB
            Booking confirmedBooking = bookingService.verifyPayment(verificationDTO);

            // <--- START EMAIL LOGIC --->
            try {
                // Check if library details exist to avoid NullPointer error
                String libName = (confirmedBooking.getLibrary() != null)
                        ? confirmedBooking.getLibrary().getName()
                        : "Library Seat";

                // ✅ FIX: Use .getUser().getEmail() instead of .getUserEmail()
                String userEmail = (confirmedBooking.getUser() != null)
                        ? confirmedBooking.getUser().getEmail()
                        : "unknown@error.com";

                emailService.sendBookingConfirmation(
                        userEmail,         // Corrected Line
                        libName,
                        confirmedBooking.getAmountPaid(),
                        confirmedBooking.getId()
                );
            } catch (Exception e) {
                // Log error so booking still succeeds even if email fails
                System.err.println("Failed to send email: " + e.getMessage());
            }
            // <--- END EMAIL LOGIC --->

            return ResponseEntity.ok(confirmedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<DashboardBookingDTO>> getUserDashboard(@RequestParam String userEmail) {
        return  ResponseEntity.ok(bookingService.getUserDashboard(userEmail));

    }
    // 3. Get Payment History
    // URL: GET http://localhost:8080/api/bookings/history?userEmail=rahul@gmail.com&libraryId=1
    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryDTO>> getHistory(
            @RequestParam String userEmail,
            @RequestParam Long libraryId) {

        return ResponseEntity.ok(bookingService.getPaymentHistory(userEmail, libraryId));
    }



    @GetMapping("/check-eligibility")
    public ResponseEntity<Boolean> checkWelcomeOfferEligibility(@RequestParam String userEmail) {
        // Returns TRUE if user is new (Show Offer), FALSE if old (Hide Offer)
        return ResponseEntity.ok(bookingService.isNewUser(userEmail));
    }
}
