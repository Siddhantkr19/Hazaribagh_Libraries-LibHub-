package com.HazaribaghLibraries.service;

import com.HazaribaghLibraries.dto.LoginRequestDTO;
import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.HazaribaghLibraries.dto.SignupRequestDTO;
@Service

public class AuthService {
     private  final ModelMapper modelMapper;
     private final UserRepository userRepository;

    public AuthService(ModelMapper modelMapper, UserRepository userRepository) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
    }
     public UserDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.getEmail()).orElseThrow(() -> new RuntimeException("User not found"));

         if(!user.getPassword().equals(loginRequestDTO.getPassword())){
             throw new RuntimeException("Invalid Password");

         }
        return modelMapper.map(user , UserDTO.class);
    }



    // --- REGISTER (NEW CODE) ---
    public UserDTO registerUser(SignupRequestDTO signupRequest) {
        // 1. Check if email exists
        if(userRepository.findByEmail(signupRequest.getEmail()).isPresent()){
            throw new RuntimeException("Email already exists!");
        }

        // 2. Convert DTO to Entity manually or using Mapper
        User user = modelMapper.map(signupRequest, User.class);

        // 3. Set Default Role if missing
        if(user.getRole() == null){
            user.setRole(User.Role.Student);
        }

        // 4. Save to Database
        User savedUser = userRepository.save(user);

        // 5. Return the clean UserDTO (no password)
        return modelMapper.map(savedUser, UserDTO.class);
    }

}
