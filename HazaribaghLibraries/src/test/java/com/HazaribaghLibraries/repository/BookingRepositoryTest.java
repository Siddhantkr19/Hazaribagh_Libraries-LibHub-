package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.Booking.BookingStatus;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
// ✅ ADD THIS LINE (Fixes the H2 Error)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookingRepositoryTest {

    @Autowired private BookingRepository bookingRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private LibraryRepository libraryRepository;

    @Test
    void testCalculateTotalRevenue() {
        // GIVEN: Create Dummy Data
        Library lib = new Library();
        lib.setName("Test Lib");
        lib.setTotalSeats(10);
        // Add defaults to prevent NullPointer/Constraints
        lib.setOriginalPrice(100.0);
        lib.setOfferPrice(50.0);
        lib.setAddress("Test Address");
        libraryRepository.save(lib);

        User user = new User();
        user.setEmail("test@demo.com");
        user.setName("Test User");
        user.setPassword("password");
        userRepository.save(user);

        // Booking 1: Paid 500
        Booking b1 = new Booking();
        b1.setUser(user);
        b1.setLibrary(lib);
        b1.setAmountPaid(500.0);
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setBookingDate(LocalDateTime.now());
        bookingRepository.save(b1);

        // Booking 2: Paid 300
        Booking b2 = new Booking();
        b2.setUser(user);
        b2.setLibrary(lib);
        b2.setAmountPaid(300.0);
        b2.setStatus(BookingStatus.CONFIRMED);
        b2.setBookingDate(LocalDateTime.now());
        bookingRepository.save(b2);

        // Booking 3: Cancelled (Should NOT be counted)
        Booking b3 = new Booking();
        b3.setUser(user);
        b3.setLibrary(lib);
        b3.setAmountPaid(1000.0);
        b3.setStatus(BookingStatus.CANCELLED);
        b3.setBookingDate(LocalDateTime.now());
        bookingRepository.save(b3);

        // WHEN: Call the custom query
        Double revenue = bookingRepository.calculateTotalRevenue();

        // THEN: 500 + 300 = 800 (Cancelled is ignored)
        assertEquals(800.0, revenue);
        System.out.println("✅ Revenue Test Passed: Expected 800.0, Got " + revenue);
    }

    @Test
    void testFindExpiringBookings() {
        // GIVEN
        Library lib = new Library();
        lib.setName("Expiring Lib");
        lib.setTotalSeats(10);
        lib.setOriginalPrice(100.0);
        lib.setOfferPrice(50.0);
        lib.setAddress("Test Address");
        libraryRepository.save(lib);

        User user = new User();
        user.setEmail("expiring@demo.com");
        user.setName("Expiring User");
        user.setPassword("password");
        userRepository.save(user);

        LocalDateTime now = LocalDateTime.now();

        // Case A: Expires in 2 days (Should be found)
        Booking b1 = new Booking();
        b1.setUser(user);
        b1.setLibrary(lib);
        b1.setStatus(BookingStatus.CONFIRMED);
        b1.setValidUntil(now.plusDays(2));
        bookingRepository.save(b1);

        // Case B: Expires in 10 days (Should NOT be found)
        Booking b2 = new Booking();
        b2.setUser(user);
        b2.setLibrary(lib);
        b2.setStatus(BookingStatus.CONFIRMED);
        b2.setValidUntil(now.plusDays(10));
        bookingRepository.save(b2);

        // WHEN: Look for bookings expiring between Now and 3 Days later
        List<Booking> expiring = bookingRepository.findExpiringBookings(now, now.plusDays(3));

        // THEN
        assertThat(expiring).hasSize(1);
        assertEquals(b1.getId(), expiring.get(0).getId());
        System.out.println("✅ Expiring Test Passed: Found correct booking.");
    }
}