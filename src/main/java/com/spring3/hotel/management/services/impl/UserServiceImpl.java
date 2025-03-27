package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.dtos.request.CreateUserRequest;
import com.spring3.hotel.management.dtos.request.UpdateUserRequest;
import com.spring3.hotel.management.dtos.request.UserRequest;
import com.spring3.hotel.management.dtos.response.UserProfileResponse;
import com.spring3.hotel.management.dtos.response.UserResponse;
import com.spring3.hotel.management.exceptions.BadRequestException;
import com.spring3.hotel.management.exceptions.NotFoundException;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.DateFormatter;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    ModelMapper modelMapper = new ModelMapper();


    @Override
    public UserResponse saveUser(UserRequest userRequest) {
        if(userRequest.getUsername() == null){
            throw new RuntimeException("Parameter username is not found in request..!!");
        } else if(userRequest.getPassword() == null){
            throw new RuntimeException("Parameter password is not found in request..!!");
        }


//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
//        String usernameFromAccessToken = userDetail.getUsername();
//
//        UserInfo currentUser = userRepository.findByUsername(usernameFromAccessToken);

        User savedUser = null;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = userRequest.getPassword();
        String encodedPassword = encoder.encode(rawPassword);

        User user = modelMapper.map(userRequest, User.class);
        user.setPassword(encodedPassword);
        if(userRequest.getId() != null){
            User oldUser = userRepository.findFirstById(userRequest.getId());
            if(oldUser != null){
                oldUser.setId(user.getId());
                oldUser.setPassword(user.getPassword());
                oldUser.setUsername(user.getUsername());
                oldUser.setRole(user.getRole());

                savedUser = userRepository.save(oldUser);
            } else {
                throw new RuntimeException("Can't find record with identifier: " + userRequest.getId());
            }
        } else {
            savedUser = userRepository.save(user);
        }
        return modelMapper.map(savedUser, UserResponse.class);
    }

    @Override
    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String usernameFromAccessToken = userDetail.getUsername();
        User user = userRepository.findByUsername(usernameFromAccessToken);
        return modelMapper.map(user, UserResponse.class);
    }

    @Override
    public List<UserResponse> getAllUser() {
        List<User> users = (List<User>) userRepository.findAll();
        Type setOfDTOsType = new TypeToken<List<UserResponse>>(){}.getType();
        return modelMapper.map(users, setOfDTOsType);
    }

    @Override
    public UserProfileResponse getUserProfile(Integer id) {
        User user = userRepository.findFirstById(id);
        if(user == null){
            throw new NotFoundException("Can't find record with identifier: " + id);
        }
        return mapToUserProfileResponse(user);
    }

    @Override
    public List<UserProfileResponse> getUserList() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(this::mapToUserProfileResponse)
            .collect(Collectors.toList());
    }

    @Override
    public Integer getUserIdByUsername(String username) {
        return userRepository.findByUsername(username).getId();
    }

    @Override
    public String getUserRoleByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return user.getRole().getName();
    }

    @Override
    public UserProfileResponse updateUser(UpdateUserRequest request, Integer id) {
        log.info("Updating user with ID: {}", id);
        User user = userRepository.findFirstById(id);
        if (user == null) {
            log.error("User not found with ID: {}", id);
            throw new NotFoundException("Can't find record with identifier: " + id);
        }
        log.info("Found user: {}", user);

        user.setFullName(request.getFullName());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setGender(request.getGender());
        log.info("Setting date of birth: {}", request.getDateOfBirth());
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }else {
            user.setDateOfBirth(null);
        }
        user.setNationalId(request.getNationalId());
        user.setUpdatedAt(LocalDateTime.now());

        log.info("Saving user: {}", user);
        User savedUser = userRepository.save(user);
        log.info("User saved successfully: {}", savedUser);

        return mapToUserProfileResponse(savedUser);
    }

    @Override
    public UserProfileResponse createUser(CreateUserRequest request) {
        User user = new User();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = request.getPassword();
        String encodedPassword = encoder.encode(rawPassword);

        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setFullName(request.getFullName());
        user.setAddress(request.getAddress());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhone());
        user.setGender(request.getGender());
        log.info("Setting date of birth: {}", request.getDateOfBirth());
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
        }else {
            user.setDateOfBirth(null);
        }
        user.setNationalId(request.getNationalId());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(request.getRole());

        log.info("Creating user: {}", user);
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser);

        return mapToUserProfileResponse(savedUser);
    }

    private UserProfileResponse mapToUserProfileResponse(User user){
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setId(user.getId());
        userProfileResponse.setUsername(user.getUsername());
        userProfileResponse.setFullName(user.getFullName());
        userProfileResponse.setEmail(user.getEmail());
        userProfileResponse.setPhone(user.getPhoneNumber());
        userProfileResponse.setAddress(user.getAddress());
        userProfileResponse.setGender(user.getGender());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
        userProfileResponse.setDateOfBirth(String.valueOf(user.getDateOfBirth()));
        userProfileResponse.setNationalId(user.getNationalId());
        userProfileResponse.setCreatedAt(user.getCreatedAt().format(formatter));
        if (user.getUpdatedAt() != null){
            userProfileResponse.setUpdatedAt(user.getUpdatedAt().format(formatter));
        }else {
            userProfileResponse.setUpdatedAt(null);
        }
        return userProfileResponse;
    }

}
