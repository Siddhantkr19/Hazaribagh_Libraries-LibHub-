package com.HazaribaghLibraries.service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetEmail(String toEmail, String token) {
        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("siddhantkumar7488@gmail.com");
        message.setTo(toEmail);
        message.setSubject("LibHub: Password Reset Request");
        message.setText("Hello,\n\n"
                + "You have requested to reset your password.\n"
                + "Click the link below to change your password:\n\n"
                + resetLink + "\n\n"
                + "This link will expire in 15 minutes.\n"
                + "Ignore this email if you do remember your password.");

        mailSender.send(message);
        System.out.println("Mail sent successfully to " + toEmail);
    }


    // 2. NEW CODE (For Scheduler / Admin Reminders)


    // We use @Async so the Scheduler doesn't freeze while waiting for email to send
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            // MimeMessage is required for sending HTML (Colors, Bold text, etc.)
            MimeMessage message = mailSender.createMimeMessage();

            // 'true' means multipart (for attachments) or HTML content
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("siddhantkumar7488@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // <--- 'true' tells Gmail this is HTML code

            mailSender.send(message);
            System.out.println("HTML Reminder sent successfully to: " + to);

        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email: " + e.getMessage());
        }
    }
}