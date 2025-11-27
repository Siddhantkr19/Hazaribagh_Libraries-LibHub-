package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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
}
