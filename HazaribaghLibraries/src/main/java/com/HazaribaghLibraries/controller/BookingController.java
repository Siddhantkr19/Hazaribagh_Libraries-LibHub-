package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.BookingRequestDTO;
import com.HazaribaghLibraries.dto.DashboardBookingDTO;
import com.HazaribaghLibraries.dto.PaymentHistoryDTO;
import com.HazaribaghLibraries.dto.PaymentVerificationDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.service.BookingService;
import com.razorpay.RazorpayException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:5173")
public class BookingController {
    private  final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
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
    // Frontend calls this AFTER Razorpay success popup closes
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDTO verificationDTO) {
        try {
            Booking confirmedBooking = bookingService.verifyPayment(verificationDTO);
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
}
