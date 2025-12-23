package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.PaymentHistory;
import com.HazaribaghLibraries.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    // 1. Fetch history for a specific user in a specific library
    List<PaymentHistory> findByUserAndLibraryOrderByPaymentDateDesc(User user, Library library);

    // 2. [OPTIMIZED] Fetch ALL history for a library using just the ID
    // This is faster because we don't need to fetch the full Library object first
    List<PaymentHistory> findByLibraryIdOrderByPaymentDateDesc(Long libraryId);

    /*
     * Alternative JPQL query (for learning purpose - not needed now)
     *
     * @Query("SELECT p FROM PaymentHistory p " +
     * "WHERE p.user = :user AND p.library = :library " +
     * "ORDER BY p.paymentDate DESC")
     * List<PaymentHistory> findPaymentsByUserAndLibrary(
     * @Param("user") User user,
     * @Param("library") Library library
     * );
     */
}