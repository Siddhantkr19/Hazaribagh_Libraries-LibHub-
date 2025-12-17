package com.HazaribaghLibraries.dto;

import lombok.Data;

@Data
public class AdminStatsDTO {
    private Double totalRevenue;
    private Long activeSeats;
    private Long totalUsers;
    private Long expiringSoonCount;
}