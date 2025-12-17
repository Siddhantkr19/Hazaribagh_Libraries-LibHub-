package com.HazaribaghLibraries.repository;
import com.HazaribaghLibraries.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email); // cheak email is already exist or not.

    String email(String email);
    Optional<User> findByResetToken(String resetToken);
    // [NEW] Find users by role (e.g. show all Admins)
    List<User> findByRole(User.Role role);

    // [NEW] Search for user (partial match for Search Bar)
    List<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
}
