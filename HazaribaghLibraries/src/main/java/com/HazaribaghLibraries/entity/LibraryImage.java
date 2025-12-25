package com.HazaribaghLibraries.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "library_images") // Explicit lowercase for Linux compatibility
public class LibraryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ The Primary Key that Aiven requires

    @Column(nullable = false, length = 1000) // Increased length for long URLs
    private String imageUrl;
}