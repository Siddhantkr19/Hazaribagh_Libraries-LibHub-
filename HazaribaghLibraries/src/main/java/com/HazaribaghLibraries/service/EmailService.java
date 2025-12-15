package com.HazaribaghLibraries.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
}