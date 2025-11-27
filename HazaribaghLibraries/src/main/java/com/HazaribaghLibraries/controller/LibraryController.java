package com.HazaribaghLibraries.controller;


import com.HazaribaghLibraries.dto.LibraryCardDTO;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.service.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/libraries") // All URLs start with this
@CrossOrigin(origins = "http://localhost:3000") // Allows React to talk to Java
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }
    // 1. Get All Libraries
    // URL: GET http://localhost:8080/api/libraries
    @GetMapping
    public ResponseEntity<List<LibraryCardDTO>> getAllLibraries() {
        return ResponseEntity.ok(libraryService.getAllLibraries());
    }

    // 2. Search Libraries (Smart Search: Name OR Price)
    // URL: GET http://localhost:8080/api/libraries/search?query=Matwari
    // URL: GET http://localhost:8080/api/libraries/search?query=500
    @GetMapping("/search")
    public ResponseEntity<List<LibraryCardDTO>> searchLibraries(@RequestParam String query) {
        return ResponseEntity.ok(libraryService.searchLibraries(query));
    }

    // 3. Get Single Library Details
    // URL: GET http://localhost:8080/api/libraries/1?userEmail=rahul@gmail.com
    // (We pass email to check for the discount logic)
    @GetMapping("/{id}")
    public ResponseEntity<LibraryCardDTO> getLibraryDetails(
            @PathVariable Long id,
            @RequestParam(required = false) String userEmail) {
        return ResponseEntity.ok(libraryService.getLibraryDetails(id, userEmail));
    }

    // 4. Create Library (For Admin - Testing purpose)
    // URL: POST http://localhost:8080/api/libraries
    @PostMapping
    public ResponseEntity<Library> createLibrary(@RequestBody Library library) {
        return ResponseEntity.ok(libraryService.createLibrary(library));
    }
}
