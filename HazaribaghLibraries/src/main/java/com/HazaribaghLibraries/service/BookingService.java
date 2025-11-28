package com.HazaribaghLibraries.service;


import com.HazaribaghLibraries.dto.DashboardBookingDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.BookingRepository;
import com.HazaribaghLibraries.repository.LibraryRepository;
import com.HazaribaghLibraries.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          LibraryRepository libraryRepository,
                          UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.libraryRepository = libraryRepository;
        this.userRepository = userRepository;
    }

    // 1 THE TRANSACTION LOGIC
    @Transactional // Ensures if anything fails, the database rolls back
    public Booking createBooking(String userEmail, Long libraryId, String paymentId) {

        // A. Find User & Library
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        Library library = libraryRepository.findById(libraryId).orElseThrow(() -> new RuntimeException("Library not found"));

        // B. Check Seat Availability (House Full Logic)
        // We count how many CONFIRMED bookings exist for this library
        // (Note: You might need to add countByLibraryAndStatus to your Repository, see note below)
        // For now, assuming simple logic:
        // if (currentBookings >= library.getTotalSeats()) throw new RuntimeException("Library is Full!");

        // C. Calculate Price (The ₹350 vs ₹400 Logic)
        boolean isOldCustomer = bookingRepository.existsByUser(user);
        Double finalPrice = isOldCustomer ? library.getOriginalPrice() : library.getOfferPrice();

        // D. Create the Booking Object
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setLibrary(library);
        booking.setBookingDate(LocalDateTime.now());
       // booking.setStartTime(LocalDateTime.now());
        booking.setValidUntil(LocalDateTime.now().plusDays(30)); // 30 Days Validity
        booking.setAmountPaid(finalPrice);
        booking.setPaymentId(paymentId);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        // E. Assign a Seat Number (Optional/Random for now)
        // Real logic would find the first empty seat. Here we just give a placeholder.
        booking.setSeatNumber("Auto-" + (System.currentTimeMillis() % 1000));

        return bookingRepository.save(booking);
    }



    // 2. THE DASHBOARD LOGIC  for the  user
    public List<DashboardBookingDTO> getUserDashboard(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findByUser(user);

        return bookings.stream().map(booking -> {
            DashboardBookingDTO dto = new DashboardBookingDTO();
            dto.setBookingId(booking.getId());
            dto.setStatus(booking.getStatus().toString());
            dto.setAmountPaid(booking.getAmountPaid());
            dto.setSeatNumber(booking.getSeatNumber());

            // Library Details
            dto.setLibraryName(booking.getLibrary().getName());
            dto.setLibraryAddress(booking.getLibrary().getAddress());
            dto.setLibraryOwnerContact(booking.getLibrary().getContactNumber());

            // Dates
            dto.setBookingDate(booking.getBookingDate());
            dto.setValidUntil(booking.getValidUntil());

            // CALCULATE DAYS REMAINING
            long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), booking.getValidUntil());
            dto.setDaysRemaining(daysLeft > 0 ? daysLeft : 0); // Never show negative days

            return dto;
        }

        ).collect(Collectors.toList());
    }

}