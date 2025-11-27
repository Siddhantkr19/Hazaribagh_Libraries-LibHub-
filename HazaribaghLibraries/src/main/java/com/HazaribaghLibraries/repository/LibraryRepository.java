package com.HazaribaghLibraries.repository;

import com.HazaribaghLibraries.entity.Library;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface LibraryRepository extends JpaRepository<Library , Long> {

    // THE SEARCH ENGINE:
    // 1) Finds libraries where Name OR Address OR LocationTag contains the keyword.
    // "IgnoreCase" ensures that "matwari" and "MATWARI" give the same result.

    List<Library> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrLocationTagContainingIgnoreCase(
            String name,
            String address,
            String locationTag
    );
    // 2. NEW Search (For Price)
    // "LessThanEqual" means if user types 500, it shows 400, 350, etc.
    List<Library> findByOriginalPriceLessThanEqual(Double maxPrice);
}
