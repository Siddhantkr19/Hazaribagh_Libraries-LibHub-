package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.HelpTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HelpTicketRepository extends JpaRepository<HelpTicket, Long> {

    // Find tickets by status (e.g., "PENDING")
    List<HelpTicket> findByStatus(String status);

    // Find tickets submitted by a specific student
    List<HelpTicket> findByUserEmail(String userEmail);

    // ✅ CHANGED: Sort by OLDEST first (Ascending order)
    List<HelpTicket> findAllByOrderByCreatedAtAsc();

    List<HelpTicket> findAllByOrderByCreatedAtDesc();
}