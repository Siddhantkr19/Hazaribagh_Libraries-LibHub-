package com.HazaribaghLibraries.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

//    @Value("${spring.mail.username}")
@Value("siddhantkumar7488@gmail.com")
    private String fromEmail;

    public void sendResetEmail(String toEmail, String token) {
        // This should point to your frontend application URL (libhub.live)
        String resetLink = "https://libhub.live/reset-password?token=" + token;


        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("LibHub: Password Reset Request");
        message.setText(
                "Hello,\n\n" +
                        "You have requested to reset your password.\n" +
                        "Click the link below to change your password:\n\n" +
                        resetLink + "\n\n" +
                        "This link will expire in 15 minutes.\n" +
                        "Ignore this email if you remember your password."
        );

        mailSender.send(message);
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Async
    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            ByteArrayOutputStream attachment,
            String fileName
    ) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            ByteArrayDataSource dataSource =
                    new ByteArrayDataSource(attachment.toByteArray(), "application/pdf");

            helper.addAttachment(fileName, dataSource);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }
    @Async
    public void sendReplyEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); // Use the configured 'from' email
        message.setTo(toEmail);
        message.setSubject("Re: " + subject + " - LibHub Support");
        message.setText(body);
        mailSender.send(message);
    }

    @Async
    public void sendBookingConfirmation(String toEmail, String libraryName, Double amount, Long bookingId) {
        String subject = "Booking Confirmed: " + libraryName;

        // You can make this HTML as fancy as you want
        String htmlBody = "<html><body>" +
                "<h2 style='color:green;'>Booking Successful! ✅</h2>" +
                "<p>Hi,</p>" +
                "<p>Your seat at <b>" + libraryName + "</b> has been successfully booked.</p>" +
                "<div style='border:1px solid #ddd; padding:10px; border-radius:5px; background-color:#f9f9f9;'>" +
                "<p><b>Booking ID:</b> #" + bookingId + "</p>" +
                "<p><b>Amount Paid:</b> ₹" + amount + "</p>" +
                "<p><b>Status:</b> <span style='color:green; font-weight:bold;'>Active</span></p>" +
                "</div>" +
                "<p>Thank you for choosing LibHub!</p>" +
                "</body></html>";

        // Reuse your existing sendHtmlEmail method
        sendHtmlEmail(toEmail, subject, htmlBody);
    }
}
