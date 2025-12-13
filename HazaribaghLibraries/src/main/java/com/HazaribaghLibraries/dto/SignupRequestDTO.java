package com.HazaribaghLibraries.dto;

import lombok.Data;

@Data
public class SignupRequestDTO {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String role;
}