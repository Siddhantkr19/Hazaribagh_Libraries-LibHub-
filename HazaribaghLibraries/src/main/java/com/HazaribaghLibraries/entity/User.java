package com.HazaribaghLibraries.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.management.relation.Role;

@Entity
@Data
@Table (name  = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String name ;

    @Column (nullable = false , unique = true)
    private String email;

//    @JsonIgnore
     private String password;

    @Column (nullable = false,unique = true)
    private String phoneNumber ;

    private String profilePicture ;

    @Enumerated(EnumType.STRING)
    private Role role ;

    public enum Role{
          Student,
         Admin
    }

}
