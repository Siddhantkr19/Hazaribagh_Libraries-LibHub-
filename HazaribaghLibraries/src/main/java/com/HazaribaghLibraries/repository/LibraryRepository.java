package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryRepository extends JpaRepository<Library , Long> {

    // THE SEARCH ENGINE:
    // Finds libraries where Name OR Address OR LocationTag contains the keyword.
    // "IgnoreCase" ensures that "matwari" and "MATWARI" give the same result.

    List<Library> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrLocationTagContainingIgnoreCase(
            String name,
            String address,
            String locationTag
    );
}
