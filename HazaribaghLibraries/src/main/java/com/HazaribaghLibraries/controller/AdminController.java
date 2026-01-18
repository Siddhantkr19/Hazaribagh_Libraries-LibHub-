package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.AdminStatsDTO;
import com.HazaribaghLibraries.entity.Booking;
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

    // 1. GET DASHBOARD STATS (Revenue, Active Seats, etc.)
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        AdminStatsDTO stats = new AdminStatsDTO();

        // A. Total Revenue
        Double revenue = bookingRepository.calculateTotalRevenue();
        stats.setTotalRevenue(revenue != null ? revenue : 0.0);

        // B. Active Seats
        stats.setActiveSeats(bookingRepository.countActiveSeats());

        // C. Total Users (Students)
        stats.setTotalUsers(userRepository.count());

        // D. Expiring Soon (Next 3 Days)
        List<Booking> expiring = bookingRepository.findExpiringBookings(
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(3)
        );
        stats.setExpiringSoonCount((long) expiring.size());

        return ResponseEntity.ok(stats);
    }

    // 2. GET ALL BOOKINGS (For the Table View)
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingRepository.findAllBookingsWithUser());
    }

//    // 3. MANUAL ACTION: Cancel a User's Booking
//    @PostMapping("/bookings/cancel/{id}")
//    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
//        Booking booking = bookingRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Booking not found"));
//
//        booking.setStatus(Booking.BookingStatus.CANCELLED);
//        bookingRepository.save(booking);
//
//        return ResponseEntity.ok("Booking cancelled successfully by Admin");
//    }
    @PostMapping("/trigger-reminders")
    public ResponseEntity<String> triggerReminders() {
        // Run the logic immediately
        schedulerService.sendExpiryReminders();
        return ResponseEntity.ok("Reminders sent successfully to expiring students.");
    }
    @GetMapping("/revenue-graph")
    public ResponseEntity<List<Map<String, Object>>> getRevenueGraphData() {
        List<Object[]> rawData = bookingRepository.getLast7DaysRevenue();

        List<Map<String, Object>> graphData = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> point = new HashMap<>();
            // row[0] is Date, row[1] is Sum
            point.put("date", row[0].toString());
            point.put("revenue", row[1]);
            graphData.add(point);
        }

        return ResponseEntity.ok(graphData);
    }

    @GetMapping("/students")
    public ResponseEntity<List<User>> getAllStudents() {
            // We assume you have a method in UserRepository or we filter here.
            // Ideally, create a method in UserRepository: List<User> findByRole(User.Role role);
            // For now, we can just fetch all and filter in Java if the repo method isn't ready.

            List<User> allUsers = userRepository.findAll();
            // Filter to show only STUDENTS (hide other Admins)
            List<User> students = allUsers.stream()
                    .filter(u -> u.getRole() == User.Role.Student) // Use your Enum value
                    .toList();

            return ResponseEntity.ok(students);
    }

    // [NEW] MANUAL OFFLINE BOOKING (Cash Payment)
    @PostMapping("/book-offline")
    public ResponseEntity<?> createOfflineBooking(@RequestBody OfflineBookingRequest request) {
            try {
                // 1. Find the Student
                User student = userRepository.findByEmail(request.getStudentEmail())
                        .orElseThrow(() -> new RuntimeException("Student email not found! Ask them to register first."));

                // 2. Find the Library
                Library library = libraryRepository.findById(request.getLibraryId())
                        .orElseThrow(() -> new RuntimeException("Library ID not found"));

                // 3. Create the Booking Manually
                Booking booking = new Booking();
                booking.setUser(student);
                booking.setLibrary(library);
                booking.setBookingDate(LocalDateTime.now());

                // Set Validity (Now + Duration)
                booking.setValidUntil(LocalDateTime.now().plusDays(request.getDurationDays()));

                // Set Payment Info
                booking.setAmountPaid(request.getAmountPaid());
                booking.setStatus(BookingStatus.CONFIRMED); // Directly Confirm it
                booking.setSeatNumber(request.getSeatNumber());

                // 4. Save
                bookingRepository.save(booking);

                return ResponseEntity.ok("Offline Booking Successful for " + student.getName());

            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error: " + e.getMessage());
            }
    }

    // [NEW] CANCEL BOOKING
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancelBookingById(@PathVariable Long id) {
            try {
                Booking booking = bookingRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Booking not found"));

                // Update Status
                booking.setStatus(BookingStatus.CANCELLED);

                // If you want to free up the seat specifically, logic can go here.
                // But usually, the "Occupied Seats" query filters out CANCELLED automatically.

                bookingRepository.save(booking);
                return ResponseEntity.ok("Booking cancelled successfully.");

            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error: " + e.getMessage());
            }
    }
    // 8. DOWNLOAD & EMAIL PAYMENT REPORT
    // ✅ URL matches Frontend: /api/admin/reports/payments
    @GetMapping("/reports/payments")
    public ResponseEntity<?> downloadPaymentReport(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long libraryId,
            @RequestParam(required = false) String sendTo
    ) {
        try {
            ByteArrayOutputStream pdfStream = paymentReportService.generatePaymentReport(email, libraryId, sendTo);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=payment-report.pdf")
                    .body(pdfStream.toByteArray());

        } catch (Exception e) {
            // ✅ Handles "User Not Found" or "Library Not Found" gracefully
            return ResponseEntity.badRequest().body("Report Generation Failed: " + e.getMessage());
        }
    }
}
