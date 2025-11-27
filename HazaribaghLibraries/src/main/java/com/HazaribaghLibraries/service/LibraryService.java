package com.HazaribaghLibraries.service;


import com.HazaribaghLibraries.ModelMapper.ModelMapperConfig;
import com.HazaribaghLibraries.dto.LibraryCardDTO;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.BookingRepository;
import com.HazaribaghLibraries.repository.LibraryRepository;
import com.HazaribaghLibraries.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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

 // Get call for all the library ;
    public List<LibraryCardDTO>  getAllLibraries(){
        List <Library> libraries  = libraryRepository.findAll();
        return libraries.stream()
                .map(library -> modelMapper.map(library, LibraryCardDTO.class)
                ).collect(Collectors.toList());
    }

    // Creat Library by Admin --- POST call

    public Library createLibrary(Library library) {
        return libraryRepository.save(library);
    }

     // Post call of libraries according to the query ;

    public List<LibraryCardDTO> searchLibraries(String query) {
        List<Library> libraries;
        if(isNumeric(query)){
            Double maxPrice = Double.parseDouble(query);
            libraries = libraryRepository.findByOriginalPriceLessThanEqual(maxPrice);

        }
         else{
             libraries  =libraryRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCaseOrLocationTagContainingIgnoreCase(query,query,query);
        }
 return libraries.stream()
                .map(library -> modelMapper.map(library, LibraryCardDTO.class)
                ).collect(Collectors.toList());
    }
     private static boolean isNumeric(String str){
        try{
            Double.parseDouble(str);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }



    // When user click on any library the user see offer price if they never login before.
     // Post methodd call


    public LibraryCardDTO getLibraryDetails(Long libraryId , String userEmail){
        Library library = libraryRepository.findById(libraryId).orElseThrow(() -> new RuntimeException("Library not found with id: " + libraryId));


        // if library found convert it into dto.

        LibraryCardDTO dto = modelMapper.map(library , LibraryCardDTO.class);

        if(userEmail!= null){
            User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

             boolean isOldCustomer = userRepository.existsByEmail(userEmail);

             if(isOldCustomer){
                 dto.setOfferPrice(dto.getOriginalPrice());
             }
        }
         return dto  ;

    }

}
