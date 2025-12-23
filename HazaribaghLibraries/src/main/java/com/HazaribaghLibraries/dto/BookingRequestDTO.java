package com.HazaribaghLibraries.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequestDTO {
    @NotNull(message = "Library ID is required")
    private Long libraryId;
}
