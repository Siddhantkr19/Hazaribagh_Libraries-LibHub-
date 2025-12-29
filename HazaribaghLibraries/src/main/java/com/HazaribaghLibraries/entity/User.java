package com.HazaribaghLibraries.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table (name  = "users")
@NoArgsConstructor
@AllArgsConstructor
@BatchSize(size = 20) // ✅ FIX: Prevents lag when Admin views list of Booking
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, message = "Name must be at least 3 characters")
    private String name ;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 numeric digits")
    private String phoneNumber ;

    private String profilePicture ;

    @Enumerated(EnumType.STRING)
    private Role role ;

    public enum Role{
        Student,
        Admin
    }
    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    // 👇 ADD THESE LISTS AT THE BOTTOM
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore // 🛑 Critical: Stops the infinite loop
    private List<Booking> bookings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore // 🛑 Critical: Stops the infinite loop
    private List<Review> reviews;

}
