package com.HazaribaghLibraries.controller;

import com.HazaribaghLibraries.dto.ApiResponse;
import com.HazaribaghLibraries.dto.LibraryCardDTO;
import com.HazaribaghLibraries.entity.Library;
import com.HazaribaghLibraries.service.LibraryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libraries") // All URLs start with this
// @CrossOrigin...  <-- REMOVED! We let SecurityConfig.java handle this globally now.
@Tag(name = "Libraries", description = "Search, Filter, and View Libraries")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    // 1. Get All Libraries
    @GetMapping
    public ResponseEntity<ApiResponse<List<LibraryCardDTO>>> getAllLibraries() {
        return ResponseEntity.ok(new ApiResponse<>( "All libraries fetched ",libraryService.getAllLibraries()));
    }

    // 2. Search Libraries (Smart Search: Name OR Price)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LibraryCardDTO>>> searchLibraries(@RequestParam String query) {
        return ResponseEntity.ok(new ApiResponse<>("Search results", libraryService.searchLibraries(query)));
    }

    // 3. Get Single Library Details
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LibraryCardDTO>> getLibraryDetails(
            @PathVariable Long id,
            @RequestParam(required = false) String userEmail) {
        return ResponseEntity.ok(new ApiResponse<>("Library details fetched", libraryService.getLibraryDetails(id, userEmail)));
    }

    // --- ADMIN ONLY ENDPOINTS ---
    // (SecurityConfig protects these URLs so only Admins can use them)

    // Admin: Create
    @PostMapping
    public ResponseEntity<ApiResponse<Library>> createLibrary(@RequestBody Library library) {
        return ResponseEntity.ok(new ApiResponse<>("Library created", libraryService.createLibrary(library)));
    }

    // Admin: Update (Edit)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Library>> updateLibrary(@PathVariable Long id, @RequestBody Library library) {
        return ResponseEntity.ok(new ApiResponse<>("Library updated", libraryService.updateLibrary(id, library)));
    }

    // Admin: Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLibrary(@PathVariable Long id) {
        libraryService.deleteLibrary(id);
        return ResponseEntity.ok(new ApiResponse<>("Library deleted successfully"));
    }
}