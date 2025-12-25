package com.HazaribaghLibraries.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "library_amenities") // Explicit lowercase for Linux compatibility
public class LibraryAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ The Primary Key that Aiven requires

    @Column(nullable = false)
    private String name; // e.g., "WiFi", "AC"
}
