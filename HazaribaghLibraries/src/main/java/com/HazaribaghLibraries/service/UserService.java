package com.HazaribaghLibraries.service;


import com.HazaribaghLibraries.dto.UserDTO;
import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }
  // register new user
    public UserDTO registerUser(User user){
         if(userRepository.findByEmail(user.getEmail()).isPresent()){
             throw new RuntimeException("Email already exists!");
         }

         if(user.getRole() == null){
             user.setRole(User.Role.Student);

         }
          return modelMapper.map(userRepository.save(user), UserDTO.class);

    }

    public  UserDTO getUserProfile(String userEmail){
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
    return modelMapper.map(user, UserDTO.class);
    }
}
