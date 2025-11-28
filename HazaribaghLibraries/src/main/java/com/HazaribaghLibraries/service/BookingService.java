package com.HazaribaghLibraries.service;


import com.HazaribaghLibraries.dto.DashboardBookingDTO;
import com.HazaribaghLibraries.dto.PaymentHistoryDTO;
import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.PaymentHistory;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.BookingRepository;
import com.HazaribaghLibraries.repository.LibraryRepository;
import com.HazaribaghLibraries.repository.PaymentHistoryRepository;
import com.HazaribaghLibraries.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.aspectj.asm.IModelFilter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final BookingRepository bookingRepository;
    private final LibraryRepository libraryRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public BookingService(PaymentHistoryRepository paymentHistoryRepository, BookingRepository bookingRepository,
                          LibraryRepository libraryRepository,
                          UserRepository userRepository, ModelMapper modelMapper) {
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.bookingRepository = bookingRepository;
        this.libraryRepository = libraryRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
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

        // 1. SAVE THE RECEIPT (PAYMENT HISTORY) - NEW STEP!
        PaymentHistory receipt = new PaymentHistory();
        receipt.setUser(user);
        receipt.setLibrary(library);
        receipt.setAmount(finalPrice);
        receipt.setPaymentId(paymentId);
        receipt.setPaymentDate(LocalDateTime.now());
        receipt.setStatus("SUCCESS");
        paymentHistoryRepository.save(receipt);  // payment done then ok other wise rollback no dont take overstress to  understand the code

        // 2. UPDATE OR CREATE BOOKING (Your Previous Logic)
        var existingBookingOpt = bookingRepository.findTopByUserAndLibraryAndStatusOrderByValidUntilDesc(
                user, library, Booking.BookingStatus.CONFIRMED);

        Booking bookingToSave;
                                                 // cheaking  user subsription expire or not
        if (existingBookingOpt.isPresent() && existingBookingOpt.get().getValidUntil().isAfter(LocalDateTime.now())) {
            // Extend Existing
            bookingToSave = existingBookingOpt.get();
            bookingToSave.setValidUntil(bookingToSave.getValidUntil().plusDays(28));
        } else {
            // Create New
            bookingToSave = new Booking();
            bookingToSave.setUser(user);
            bookingToSave.setLibrary(library);
            bookingToSave.setBookingDate(LocalDateTime.now());
            bookingToSave.setValidUntil(LocalDateTime.now().plusDays(28));
            bookingToSave.setSeatNumber("Auto-" + (System.currentTimeMillis() % 1000));
        }

        // Common Updates
        bookingToSave.setAmountPaid(finalPrice);
        bookingToSave.setPaymentId(paymentId);
        bookingToSave.setStatus(Booking.BookingStatus.CONFIRMED);

        return bookingRepository.save(bookingToSave);
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

    // 3. GET HISTORY METHOD
    public List<PaymentHistoryDTO> getPaymentHistory(String userEmail, Long libraryId) {

        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        Library library = libraryRepository.findById(libraryId).orElseThrow(() -> new RuntimeException("Library not found"));

        // 1. Fetch all Entities
        List<PaymentHistory> historyList = paymentHistoryRepository
                .findByUserAndLibraryOrderByPaymentDateDesc(user, library);

        // 2. Convert to DTOs by model mapper
        return historyList.stream().map(history ->   {
                    PaymentHistoryDTO dto = modelMapper.map(history, PaymentHistoryDTO.class);
                    return dto;
                })
                .collect(Collectors.toList());
    }


}