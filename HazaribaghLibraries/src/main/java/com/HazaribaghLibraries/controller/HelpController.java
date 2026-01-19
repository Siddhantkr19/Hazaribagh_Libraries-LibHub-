package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ApiResponse; // ✅ Import
import com.HazaribaghLibraries.entity.HelpTicket;
import com.HazaribaghLibraries.repository.HelpTicketRepository;
import com.HazaribaghLibraries.service.EmailService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/help")
@Tag(name = "Help Desk", description = "Student Support Tickets")
public class HelpController {

    @Autowired
    private HelpTicketRepository helpTicketRepository;
    @Autowired
    private EmailService emailService;

    // 1. Submit Ticket
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<String>> submitTicket(@RequestBody HelpTicket ticket) {
        helpTicketRepository.save(ticket);
        return ResponseEntity.ok(new ApiResponse<>("Message saved successfully."));
    }

    // 2. Get All Tickets
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<HelpTicket>>> getAllTickets() {
        return ResponseEntity.ok(new ApiResponse<>("All tickets fetched", helpTicketRepository.findAllByOrderByCreatedAtDesc()));
    }

    // 3. Reply to Ticket
    @PostMapping("/reply")
    public ResponseEntity<ApiResponse<String>> replyToTicket(@RequestBody Map<String, String> request) {
        String ticketId = request.get("ticketId");
        String message = request.get("message");

        HelpTicket ticket = helpTicketRepository.findById(Long.parseLong(ticketId))
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        emailService.sendReplyEmail(ticket.getUserEmail(), ticket.getSubject(), message);

        ticket.setStatus("REPLIED");
        helpTicketRepository.save(ticket);

        return ResponseEntity.ok(new ApiResponse<>("Reply sent successfully!"));
    }

    // 4. Delete Ticket
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTicket(@PathVariable Long id) {
        helpTicketRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>("Ticket deleted successfully."));
    }
}