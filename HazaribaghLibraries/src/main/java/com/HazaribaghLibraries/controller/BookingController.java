package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.BookingRequestDTO;
import com.HazaribaghLibraries.dto.DashboardBookingDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
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

}
