package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.dto.LibraryCardDTO;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.LibraryAmenity;
import com.HazaribaghLibraries.entity.LibraryImage;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.BookingRepository;
import com.HazaribaghLibraries.repository.LibraryRepository;
import com.HazaribaghLibraries.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    private final LibraryRepository libraryRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public LibraryService(LibraryRepository libraryRepository, BookingRepository bookingRepository, UserRepository userRepository, ModelMapper modelMapper) {
        this.libraryRepository = libraryRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    // 1. GET ALL LIBRARIES
    public List<LibraryCardDTO> getAllLibraries() {
        List<Library> libraries = libraryRepository.findAll();
        return libraries.stream()
                .map(library -> modelMapper.map(library, LibraryCardDTO.class))
                .collect(Collectors.toList());
    }

    // 2. SEARCH LIBRARIES
    public List<LibraryCardDTO> searchLibraries(String query) {
        List<Library> libraries;
        if (isNumeric(query)) {
            Double maxPrice = Double.parseDouble(query);
            libraries = libraryRepository.findByOriginalPriceLessThanEqual(maxPrice);
        } else {
            libraries = libraryRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrLocationTagContainingIgnoreCase(query, query, query);
        }
        return libraries.stream()
                .map(library -> modelMapper.map(library, LibraryCardDTO.class))
                .collect(Collectors.toList());
    }

    // 3. GET LIBRARY DETAILS (With "Old Customer" Check)
    public LibraryCardDTO getLibraryDetails(Long libraryId, String userEmail) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Library not found with id: " + libraryId));

        LibraryCardDTO dto = modelMapper.map(library, LibraryCardDTO.class);

        if (userEmail != null && !userEmail.equals("null") && !userEmail.isEmpty()) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

            // ✅ FIX: Check if they have BOOKINGS, not just if they exist
            boolean isOldCustomer = bookingRepository.existsByUser(user);

            if (isOldCustomer) {
                // If they booked before, they pay the original price (no offer)
                dto.setOfferPrice(dto.getOriginalPrice());
            }
        }
        return dto;
    }

    // --- ADMIN FUNCTIONS ---

    // 4. CREATE LIBRARY (Safe conversion for Images/Amenities)
    public Library createLibrary(Library libraryInput) {
        // NOTE: If you are sending DTOs, ensure your Controller maps them to Entity first.
        // Or better, use the logic I gave previously to map Strings -> Entities.
        // Assuming your Controller handles DTO -> Entity mapping now, we just save.
        return libraryRepository.save(libraryInput);
    }

    // 5. UPDATE LIBRARY (Database Safe)
    @Transactional // ✅ Important for updates
    public Library updateLibrary(Long id, Library libraryDetails) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library not found with id: " + id));

        // Update Basic Fields
        library.setName(libraryDetails.getName());
        library.setAddress(libraryDetails.getAddress());
        library.setLocationTag(libraryDetails.getLocationTag());
        library.setDescription(libraryDetails.getDescription());
        library.setOriginalPrice(libraryDetails.getOriginalPrice());
        library.setOfferPrice(libraryDetails.getOfferPrice());
        library.setOpeningHours(libraryDetails.getOpeningHours());
        library.setTotalSeats(libraryDetails.getTotalSeats());
        library.setContactNumber(libraryDetails.getContactNumber());

        // ✅ FIX: Update Amenities Safely (Clear + AddAll)
        if (libraryDetails.getAmenities() != null) {
            library.getAmenities().clear();
            library.getAmenities().addAll(libraryDetails.getAmenities());
        }

        // ✅ FIX: Update Images Safely (Clear + AddAll)
        if (libraryDetails.getImages() != null) {
            library.getImages().clear();
            library.getImages().addAll(libraryDetails.getImages());
        }

        return libraryRepository.save(library);
    }

    // 6. DELETE LIBRARY
    public void deleteLibrary(Long id) {
        if (!libraryRepository.existsById(id)) {
            throw new RuntimeException("Library not found with id: " + id);
        }
        libraryRepository.deleteById(id);
    }

    // Helper
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}