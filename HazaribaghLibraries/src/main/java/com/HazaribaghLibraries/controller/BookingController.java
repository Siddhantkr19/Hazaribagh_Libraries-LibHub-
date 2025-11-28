package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.BookingRequestDTO;
import com.HazaribaghLibraries.dto.DashboardBookingDTO;
import com.HazaribaghLibraries.dto.PaymentHistoryDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:3000")
public class BookingController {
    private  final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }


     @PostMapping("/initiate")
     public ResponseEntity<Booking>  createBooking(@RequestParam String userEmail, @RequestBody BookingRequestDTO request) {

         String fakepaymentid = "PAY-" + System.currentTimeMillis();
         Booking booking = bookingService.createBooking(userEmail, request.getLibraryId(), fakepaymentid);
        return ResponseEntity.ok(booking);
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
