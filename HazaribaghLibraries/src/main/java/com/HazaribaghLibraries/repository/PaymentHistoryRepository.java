package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.PaymentHistory;
import com.HazaribaghLibraries.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    // i also use like this but try to understand new jpa query :

//    @Query("SELECT p FROM PaymentHistory p " +
//            "WHERE p.user = :user AND p.library = :library " +
//            "ORDER BY p.paymentDate DESC")
//    List<PaymentHistory> findPaymentsByUserAndLibrary(
//            @Param("user") User user,
//            @Param("library") Library library
//    );
//}




List<PaymentHistory> findByUserAndLibraryOrderByPaymentDateDesc(User user, Library library);
}