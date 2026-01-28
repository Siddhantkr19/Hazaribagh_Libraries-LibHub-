package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // ✅ ADD THIS

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByUser(User user);

    @Query("SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.bookingDate DESC")
    List<Booking> findByUser(@Param("user") User user);


    @Query("SELECT b FROM Booking b WHERE b.user = :user AND b.status = :status ORDER BY b.bookingDate DESC")
    List<Booking> findByUserAndStatus(@Param("user") User user, @Param("status") Booking.BookingStatus status);

    Optional<Booking> findTopByUserAndLibraryAndStatusOrderByValidUntilDesc(
            User user,
            Library library,
            Booking.BookingStatus status
    );

    Optional<Booking> findByRazorpayOrderId(String razorpayOrderId);

    // --- ADMIN DASHBOARD QUERIES ---

    @Query("SELECT SUM(b.amountPaid) FROM Booking b WHERE b.status = 'CONFIRMED'")
    Double calculateTotalRevenue();

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = 'CONFIRMED' AND b.validUntil > CURRENT_TIMESTAMP")
    Long countActiveSeats();

    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND b.validUntil BETWEEN :now AND :threeDaysLater")
    List<Booking> findExpiringBookings(@Param("now") LocalDateTime now, @Param("threeDaysLater") LocalDateTime threeDaysLater);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user ORDER BY b.bookingDate DESC")
    List<Booking> findAllBookingsWithUser();

    // Ensure your Entity class @Table(name="bookings") matches this query!
    @Query(value = "SELECT DATE(booking_date) as date, SUM(amount_paid) as total " +
            "FROM bookings " +
            "WHERE status = 'CONFIRMED' " +
            "GROUP BY DATE(booking_date) " +
            "ORDER BY date ASC " +
            "LIMIT 7", nativeQuery = true)
    List<Object[]> getLast7DaysRevenue();

    List<Booking> findByLibraryIdAndStatus(Long libraryId, Booking.BookingStatus bookingStatus);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.library.id = :libraryId AND b.status = 'CONFIRMED' AND b.validUntil > CURRENT_TIMESTAMP")
    int countActiveBookingsByLibrary(@Param("libraryId") Long libraryId);
}