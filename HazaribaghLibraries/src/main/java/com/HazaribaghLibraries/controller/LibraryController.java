package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.LibraryCardDTO;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.service.LibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libraries") // All URLs start with this
// @CrossOrigin...  <-- REMOVED! We let SecurityConfig.java handle this globally now.
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // 1. Get All Libraries
    @GetMapping
    public ResponseEntity<List<LibraryCardDTO>> getAllLibraries() {
        return ResponseEntity.ok(libraryService.getAllLibraries());
    }

    // 2. Search Libraries (Smart Search: Name OR Price)
    @GetMapping("/search")
    public ResponseEntity<List<LibraryCardDTO>> searchLibraries(@RequestParam String query) {
        return ResponseEntity.ok(libraryService.searchLibraries(query));
    }

    // 3. Get Single Library Details
    @GetMapping("/{id}")
    public ResponseEntity<LibraryCardDTO> getLibraryDetails(
            @PathVariable Long id,
            @RequestParam(required = false) String userEmail) {
        return ResponseEntity.ok(libraryService.getLibraryDetails(id, userEmail));
    }

    // --- ADMIN ONLY ENDPOINTS ---
    // (SecurityConfig protects these URLs so only Admins can use them)

    // Admin: Create
    @PostMapping
    public ResponseEntity<Library> createLibrary(@RequestBody Library library) {
        return ResponseEntity.ok(libraryService.createLibrary(library));
    }

    // Admin: Update (Edit)
    @PutMapping("/{id}")
    public ResponseEntity<Library> updateLibrary(@PathVariable Long id, @RequestBody Library library) {
        return ResponseEntity.ok(libraryService.updateLibrary(id, library));
    }

    // Admin: Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLibrary(@PathVariable Long id) {
        libraryService.deleteLibrary(id);
        return ResponseEntity.noContent().build();
    }
}