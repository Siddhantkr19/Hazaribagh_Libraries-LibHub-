package com.HazaribaghLibraries.service;


import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final String UPLOAD_DIR = "uploads/";

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }
  // register new user  && SignUp New User
    public UserDTO registerUser(User user){
         if(userRepository.findByEmail(user.getEmail()).isPresent()){
             throw new RuntimeException("Email already exists!");
         }

         if(user.getRole() == null){
             user.setRole(User.Role.Student);

         }
          return modelMapper.map(userRepository.save(user), UserDTO.class);

    }
   //  cheak login
    public  UserDTO getUserProfile(String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
    return modelMapper.map(user, UserDTO.class);
    }



    // Upload Profile Picture

    public UserDTO uploadProfilePicture(String userEmail, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Create Folder if missing
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. Generate Filename
        String originalFilename = file.getOriginalFilename();
        // Add UUID to prevent duplicate names
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // 3. Save File
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // 4. Save URL to Database (Must start with /uploads/)
        String imageUrl = "/uploads/" + uniqueFilename;

        user.setProfilePicture(imageUrl);
        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDTO.class);
    }
}
