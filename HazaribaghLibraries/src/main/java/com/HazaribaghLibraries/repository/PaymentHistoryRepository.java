package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.PaymentHistory;
import com.HazaribaghLibraries.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    // 1. Fetch history for a specific user in a specific library
    List<PaymentHistory> findByUserAndLibraryOrderByPaymentDateDesc(User user, Library library);

    // 2. [OPTIMIZED] Fetch ALL history for a library using just the ID
    // This is faster because we don't need to fetch the full Library object first
    List<PaymentHistory> findByLibraryIdOrderByPaymentDateDesc(Long libraryId);

    // --- NEW METHODS TO SOLVE N+1 PROBLEM ---

    // 3. [FULLY OPTIMIZED] Fetch for a user and library, including all details in one query.
    @Query("SELECT p FROM PaymentHistory p JOIN FETCH p.user JOIN FETCH p.library WHERE p.user = :user AND p.library = :library ORDER BY p.paymentDate DESC")
    List<PaymentHistory> findByUserAndLibraryWithDetails(User user, Library library);

    // 4. [FULLY OPTIMIZED] Fetch for a library ID, including all details in one query.
    @Query("SELECT p FROM PaymentHistory p JOIN FETCH p.user JOIN FETCH p.library WHERE p.library.id = :libraryId ORDER BY p.paymentDate DESC")
    List<PaymentHistory> findByLibraryIdWithDetails(Long libraryId);

    // 5. [FULLY OPTIMIZED] Fetch ALL history, including all details in one query.
    @Query("SELECT p FROM PaymentHistory p JOIN FETCH p.user JOIN FETCH p.library ORDER BY p.paymentDate DESC")
    List<PaymentHistory> findAllWithDetails();
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