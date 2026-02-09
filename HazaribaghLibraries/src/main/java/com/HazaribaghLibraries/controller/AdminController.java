package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.AdminStatsDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.dto.ApiResponse;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.BookingRepository;
import com.HazaribaghLibraries.repository.UserRepository;
import com.HazaribaghLibraries.service.PaymentReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.HazaribaghLibraries.service.SchedulerService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import com.HazaribaghLibraries.dto.OfflineBookingRequest;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.Booking.BookingStatus;
import com.HazaribaghLibraries.repository.LibraryRepository;
@RestController
@RequestMapping("/api/admin")
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@Tag(name = "Admin Control", description = "Revenue Stats & Management")
public class AdminController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SchedulerService schedulerService;
    private final LibraryRepository libraryRepository;
    private final PaymentReportService paymentReportService;

    public AdminController(BookingRepository bookingRepository, UserRepository userRepository, SchedulerService schedulerService, LibraryRepository libraryRepository, PaymentReportService paymentReportService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.schedulerService = schedulerService;
        this.libraryRepository = libraryRepository;
        this.paymentReportService = paymentReportService;
    }

    // 1. Dashboard Stats
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsDTO>> getDashboardStats() {
        AdminStatsDTO stats = new AdminStatsDTO();
        Double revenue = bookingRepository.calculateTotalRevenue();
        // ✅ Round to 2 decimal places
        if (revenue != null) {
            revenue = Math.round(revenue * 100.0) / 100.0;
        } else {
            revenue = 0.0;
        }
        stats.setTotalRevenue(revenue != null ? revenue : 0.0);
        stats.setActiveSeats(bookingRepository.countActiveSeats());
        stats.setTotalUsers(userRepository.count());
        List<Booking> expiring = bookingRepository.findExpiringBookings(LocalDateTime.now(), LocalDateTime.now().plusDays(3));
        stats.setExpiringSoonCount((long) expiring.size());

        return ResponseEntity.ok(new ApiResponse<>("Stats fetched", stats));
    }

    // 2. All Bookings
    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getAllBookings() {
        return ResponseEntity.ok(new ApiResponse<>("All bookings fetched", bookingRepository.findAllBookingsWithUser()));
    }

    // 3. Trigger Reminders
    @PostMapping("/trigger-reminders")
    public ResponseEntity<ApiResponse<String>> triggerReminders() {
        schedulerService.sendExpiryReminders();
        return ResponseEntity.ok(new ApiResponse<>("Reminders sent successfully"));
    }

    // 4. Revenue Graph
    @GetMapping("/revenue-graph")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getRevenueGraphData() {
        List<Object[]> rawData = bookingRepository.getLast7DaysRevenue();
        List<Map<String, Object>> graphData = new ArrayList<>();
        for (Object[] row : rawData) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", row[0].toString());
            point.put("revenue", row[1]);
            graphData.add(point);
        }
        return ResponseEntity.ok(new ApiResponse<>("Graph data fetched", graphData));
    }

    // 5. Get Students
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<User>>> getAllStudents() {
        List<User> allUsers = userRepository.findAll();
        List<User> students = allUsers.stream().filter(u -> u.getRole() == User.Role.Student).toList();
        return ResponseEntity.ok(new ApiResponse<>("Students fetched", students));
    }

    // 6. Offline Booking
    @PostMapping("/book-offline")
    public ResponseEntity<ApiResponse<String>> createOfflineBooking(@RequestBody OfflineBookingRequest request) {
        User student = userRepository.findByEmail(request.getStudentEmail())
                .orElseThrow(() -> new RuntimeException("Student email not found! Ask them to register first."));
        Library library = libraryRepository.findById(request.getLibraryId())
                .orElseThrow(() -> new RuntimeException("Library ID not found"));

        Booking booking = new Booking();
        booking.setUser(student);
        booking.setLibrary(library);
        booking.setBookingDate(LocalDateTime.now());
        booking.setValidUntil(LocalDateTime.now().plusDays(request.getDurationDays()));
        booking.setAmountPaid(request.getAmountPaid());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setSeatNumber(request.getSeatNumber());
        bookingRepository.save(booking);

        return ResponseEntity.ok(new ApiResponse<>("Offline Booking Successful for " + student.getName()));
    }

    // 7. Cancel Booking
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelBookingById(@PathVariable Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        return ResponseEntity.ok(new ApiResponse<>("Booking cancelled successfully"));
    }

    // 8. DOWNLOAD REPORT (❌ DO NOT WRAP THIS!)
    // PDF files are binary, not JSON.
    @GetMapping("/reports/payments")
    public ResponseEntity<?> downloadPaymentReport(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long libraryId,
            @RequestParam(required = false) String sendTo
    ) {
        ByteArrayOutputStream pdfStream = paymentReportService.generatePaymentReport(email, libraryId, sendTo);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=payment-report.pdf")
                .body(pdfStream.toByteArray());
    }
}