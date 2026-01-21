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
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable("libraries_v2")
    public List<LibraryCardDTO> getAllLibraries() {
        List<Library> libraries = libraryRepository.findAll();
        // Use the helper method to ensure clean strings
        return libraries.stream()
                .map(this::convertToDTO)
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
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 3. GET LIBRARY DETAILS (With "Old Customer" Check)
    public LibraryCardDTO getLibraryDetails(Long libraryId, String userEmail) {
        Library library = libraryRepository.findById(libraryId)
                .orElseThrow(() -> new RuntimeException("Library not found with id: " + libraryId));

        // Use the helper method here too
        LibraryCardDTO dto = convertToDTO(library);

        if (userEmail != null && !userEmail.equals("null") && !userEmail.isEmpty()) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

            boolean isOldCustomer = bookingRepository.existsByUser(user);

            if (isOldCustomer) {
                dto.setOfferPrice(dto.getOriginalPrice());
            }
        }
        return dto;
    }

    // --- 🛠️ HELPER: Converts Entity to Clean DTO ---
    private LibraryCardDTO convertToDTO(Library library) {
        LibraryCardDTO dto = modelMapper.map(library, LibraryCardDTO.class);

        // 1. CLEAN AMENITIES: Extract just the name string (e.g. "Wi-Fi")
        if (library.getAmenities() != null) {
            List<String> cleanAmenities = library.getAmenities().stream()
                    .map(LibraryAmenity::getName) // Only get the Name
                    .collect(Collectors.toList());
            dto.setAmenities(cleanAmenities);
        }

        // 2. CLEAN IMAGES: Extract just the URL string
        if (library.getImages() != null) {
            List<String> cleanImages = library.getImages().stream()
                    .map(LibraryImage::getImageUrl) // Only get the URL
                    .collect(Collectors.toList());
            dto.setImages(cleanImages);
        }

        return dto;
    }

    // --- ADMIN FUNCTIONS ---

    public Library createLibrary(Library libraryInput) {
        return libraryRepository.save(libraryInput);
    }

    @Transactional
    public Library updateLibrary(Long id, Library libraryDetails) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Library not found with id: " + id));

        library.setName(libraryDetails.getName());
        library.setAddress(libraryDetails.getAddress());
        library.setLocationTag(libraryDetails.getLocationTag());
        library.setDescription(libraryDetails.getDescription());
        library.setOriginalPrice(libraryDetails.getOriginalPrice());
        library.setOfferPrice(libraryDetails.getOfferPrice());
        library.setOpeningHours(libraryDetails.getOpeningHours());
        library.setTotalSeats(libraryDetails.getTotalSeats());
        library.setContactNumber(libraryDetails.getContactNumber());

        if (libraryDetails.getAmenities() != null) {
            library.getAmenities().clear();
            library.getAmenities().addAll(libraryDetails.getAmenities());
        }

        if (libraryDetails.getImages() != null) {
            library.getImages().clear();
            library.getImages().addAll(libraryDetails.getImages());
        }

        return libraryRepository.save(library);
    }

    public void deleteLibrary(Long id) {
        if (!libraryRepository.existsById(id)) {
            throw new RuntimeException("Library not found with id: " + id);
        }
        libraryRepository.deleteById(id);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}