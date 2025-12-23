package com.HazaribaghLibraries.dto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LibraryCardDTO {
    private Long id;
    private String name;
    private String address;
    private String locationTag; // e.g. "MATWARI"
    private String openingHours; // e.g. "6 AM - 10 PM"

    // Pricing Info
    private Double originalPrice; // ₹400
    private Double offerPrice;    // ₹350 (First Month)

    // Owner Info (Visible as requested)
    private Integer totalSeats;
    private String ownerContact;

    // Facilities & Photos
    // ✅ Initialize lists to prevent nulls
    private List<String> amenities = new ArrayList<>();
    private List<String> images = new ArrayList<>();
}