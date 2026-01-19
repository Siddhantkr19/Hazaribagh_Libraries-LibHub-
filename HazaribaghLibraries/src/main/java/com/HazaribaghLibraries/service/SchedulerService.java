package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.entity.Booking;
import com.HazaribaghLibraries.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
public class SchedulerService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    public SchedulerService(BookingRepository bookingRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    // Runs every day at 10:00 AM
    // Cron format: Seconds Minutes Hours DayOfMonth Month DayOfWeek
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendExpiryReminders() {
        log.info("---⏰Running Daily Expiry Check ---");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);

        // Fetch bookings expiring in the next 3 days
        List<Booking> expiringBookings = bookingRepository.findExpiringBookings(now, threeDaysLater);

        for (Booking booking : expiringBookings) {
            String studentEmail = booking.getUser().getEmail();
            String studentName = booking.getUser().getName();
            String libraryName = booking.getLibrary().getName();

            // ✅ ADD THIS LOG: Records exactly who is getting the email
            log.info("Sending expiry reminder to student: {} for library: {}", studentEmail, libraryName);
            // Build the Email Content
            String subject = "⚠️ Urgent: Your Seat at " + libraryName + " expires soon!";
            String body = buildEmailTemplate(studentName, libraryName, booking.getValidUntil());

            // Send
            emailService.sendHtmlEmail(studentEmail, subject, body);
        }

        log.info("--- Finished. Sent " + expiringBookings.size() + " reminders. ---");
    }

    private String buildEmailTemplate(String name, String libName, LocalDateTime date) {
        return "<html>" +
                "<body style='font-family: Arial, sans-serif; color: #333;'>" +
                "<div style='background-color: #f8f9fa; padding: 20px; border-radius: 10px;'>" +
                "<h2 style='color: #d9534f;'>Your Subscription is Ending!</h2>" +
                "<p>Hi <strong>" + name + "</strong>,</p>" +
                "<p>This is a friendly reminder that your seat at <strong>" + libName + "</strong> is valid until:</p>" +
                "<h3 style='background-color: #fff; padding: 10px; display: inline-block; border: 1px solid #ddd;'>" +
                date.toLocalDate() + "</h3>" +
                "<p>To keep your seat number, please renew before the expiry date.</p>" +
                "<a href='http://localhost:5173/dashboard' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Renew Now</a>" +
                "<br><br><p>Happy Studying,<br>LibHub Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}