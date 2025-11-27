package com.HazaribaghLibraries.repository;
import com.HazaribaghLibraries.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email); // cheak email is already exist or not.
}
