package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.User;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Registered
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // You can add custom query methods here if needed
    // THE DISCOUNT LOGIC:
    // If this returns true, the user is an Old Customer (Price = 400).
    // If this returns false, the user is New (Price = 350).
    boolean existsByUser(User user);

    // THE DASHBOARD HISTORY:
    // Fetches all bookings for a specific student (Active & Expired)
    List<Booking> findByUser(User user);

    // OPTIONAL: Find only active bookings (Where status is CONFIRMED)
    List<Booking> findByUserAndStatus(User user, Booking.BookingStatus status);

    // Find the if the   user  already book specific library
    Optional<Booking> findTopByUserAndLibraryAndStatusOrderByValidUntilDesc(
            User user,
            Library library,
            Booking.BookingStatus status
    );
    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    // --- ADMIN DASHBOARD QUERIES ---

    // 1. Total Revenue (Sum of all CONFIRMED payments)
    @Query("SELECT SUM(b.amountPaid) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double calculateTotalRevenue();

    // 2. Count Active Seats (Confirmed bookings that haven't expired yet)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.validUntil > CURRENT_TIMESTAMP")
    Long countActiveSeats();

    // 3. Find Expiring Soon (For the "3 Days Left" alert)
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND b.validUntil BETWEEN :now AND :threeDaysLater")
    List<Booking> findExpiringBookings(@Param("now") LocalDateTime now, @Param("threeDaysLater") LocalDateTime threeDaysLater);

    // 4. Get All Bookings with User Info (For the Admin Table)
    @Query("SELECT b FROM Booking b JOIN FETCH b.user ORDER BY b.bookingDate DESC")
    List<Booking> findAllBookingsWithUser();

    // [NEW] Get Revenue for the Last 7 Days (Grouped by Date)
    @Query(value = "SELECT DATE(booking_date) as date, SUM(amount_paid) as total " +
            "FROM bookings " +
            "WHERE status = 'CONFIRMED' " +
            "GROUP BY DATE(booking_date) " +
            "ORDER BY date ASC " +
            "LIMIT 7", nativeQuery = true)
    List<Object[]> getLast7DaysRevenue();

    List<Booking> findByLibraryIdAndStatus(Long libraryId, Booking.BookingStatus bookingStatus);
}