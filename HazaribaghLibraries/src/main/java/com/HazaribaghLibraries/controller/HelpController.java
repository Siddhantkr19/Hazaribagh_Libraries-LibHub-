package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.entity.HelpTicket;
import com.HazaribaghLibraries.repository.HelpTicketRepository;
import com.HazaribaghLibraries.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/help")
@CrossOrigin("http://localhost:5173") // Allow your React Frontend
public class HelpController {

    @Autowired
    private HelpTicketRepository helpTicketRepository;
    @Autowired
    private EmailService emailService;

    // 1. SAVE TO DATABASE (Used by Student)
    // URL: POST http://localhost:8080/api/help/submit
    @PostMapping("/submit")
    public ResponseEntity<?> submitTicket(@RequestBody HelpTicket ticket) {
        try {
            // ✅ This line permanently writes the data to the DB table
            helpTicketRepository.save(ticket);
            return ResponseEntity.ok("Message saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving to database: " + e.getMessage());
        }
    }

    // 2. RETRIEVE FROM DATABASE (Used by Admin Panel)
    // URL: GET http://localhost:8080/api/help/all
    @GetMapping("/all")
    public ResponseEntity<List<HelpTicket>> getAllTickets() {
        // ✅ Fetches all saved messages, newest first
        return ResponseEntity.ok(helpTicketRepository.findAllByOrderByCreatedAtDesc());
    }
    // 3. REPLY TO TICKET (Sends Email)
    @PostMapping("/reply")
    public ResponseEntity<?> replyToTicket(@RequestBody Map<String, String> request) {
        String ticketId = request.get("ticketId");
        String message = request.get("message");

        HelpTicket ticket = helpTicketRepository.findById(Long.parseLong(ticketId))
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Send Email
        emailService.sendReplyEmail(ticket.getUserEmail(), ticket.getSubject(), message);

        // Update Status
        ticket.setStatus("REPLIED");
        helpTicketRepository.save(ticket);

        return ResponseEntity.ok("Reply sent successfully!");
    }

    // 4. DELETE TICKET
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        helpTicketRepository.deleteById(id);
        return ResponseEntity.ok("Ticket deleted successfully.");
    }

}