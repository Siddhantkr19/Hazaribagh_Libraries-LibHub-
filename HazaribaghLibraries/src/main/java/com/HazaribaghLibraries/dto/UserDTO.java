package com.HazaribaghLibraries.dto;

import com.HazaribaghLibraries.entity.User;
import lombok.Data;

@Data
public class UserDTO {

    private Long id;
    private String name;
    private String email;

    // VISIBLE FOR DEBUGGING ONLY (Remove before final demo)
    private String password;

    private String phoneNumber;
    private String profilePictureUrl;
    private User.Role role; // STUDENT or ADMIN
}
