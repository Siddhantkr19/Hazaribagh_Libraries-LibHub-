package com.HazaribaghLibraries.dto;

import lombok.Data;

@Data
public class ReviewRequestDTO {

    private String userEmail;
    private Long bookingId;
    private Integer rating;
    private String comment;
}
