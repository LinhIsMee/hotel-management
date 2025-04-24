package com.spring3.hotel.management.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.spring3.hotel.management.dto.request.CreateUserRequest;
import com.spring3.hotel.management.dto.request.RegisterRequest;
import com.spring3.hotel.management.dto.request.UpdateUserRequest;
import com.spring3.hotel.management.dto.request.UserRequest;
import com.spring3.hotel.management.dto.response.UserProfileResponse;
import com.spring3.hotel.management.dto.response.UserResponse;
import com.spring3.hotel.management.exceptions.BadRequestException;
import com.spring3.hotel.management.exceptions.DuplicateResourceException;
import com.spring3.hotel.management.exceptions.NotFoundException;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.EmailService;
import com.spring3.hotel.management.services.UserService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;
    
    @Autowired(required = false)
    EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    ModelMapper modelMapper = new ModelMapper();

    @Override
    public UserResponse registerUser(RegisterRequest registerRequest) {
        // Check for existing username
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        
        // Check for existing email
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        
        // Set default role to "CUSTOMER" (String)
        user.setRole("CUSTOMER"); 
        
        User savedUser = userRepository.save(user);
        
        // Tạo response
        return UserResponse.builder()
                .id(savedUser.getId().toString())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole()) // Lấy role kiểu String
                .active(true) // Giả sử mặc định là active
                .build();
    }

    @Override
    public UserResponse saveUser(UserRequest userRequest) {
        // Kiểm tra các trường bắt buộc
        if(userRequest.getUsername() == null){
            throw new RuntimeException("Parameter username is not found in request..!!");
        } else if(userRequest.getPassword() == null){
            throw new RuntimeException("Parameter password is not found in request..!!");
        }

        User savedUser = null;
        User user = modelMapper.map(userRequest, User.class); // Map từ DTO sang Entity
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        
        // Sử dụng role kiểu String từ request hoặc đặt mặc định "CUSTOMER"
        user.setRole(userRequest.getRole() != null ? userRequest.getRole() : "CUSTOMER");
        
        if(userRequest.getId() != null){
            User oldUser = userRepository.findFirstById(userRequest.getId());
            if(oldUser != null){
                oldUser.setId(user.getId());
                oldUser.setPassword(user.getPassword());
                oldUser.setUsername(user.getUsername());
                oldUser.setEmail(user.getEmail());
                oldUser.setFirstName(user.getFirstName());
                oldUser.setLastName(user.getLastName());
                oldUser.setPhoneNumber(user.getPhoneNumber()); // Thêm cập nhật SĐT
                oldUser.setRole(user.getRole()); // Cập nhật role
                savedUser = userRepository.save(oldUser);
            } else {
                throw new RuntimeException("Can't find record with identifier: " + userRequest.getId());
            }
        } else {
            // Kiểm tra trùng lặp cho user mới
            if (userRepository.existsByUsername(user.getUsername())) {
                throw new DuplicateResourceException("Username already exists");
            }
            if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
                 throw new DuplicateResourceException("Email already exists");
            }
            savedUser = userRepository.save(user);
        }
        
        // Tạo response
        return UserResponse.builder()
                .id(savedUser.getId().toString())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole()) // Lấy role kiểu String
                .active(true) // Giả sử active
                .build();
    }

    @Override
    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String usernameFromAccessToken = userDetail.getUsername();
        User user = userRepository.findByUsername(usernameFromAccessToken);
        
        // Tạo response
        return UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole()) // Lấy role kiểu String
                .active(true) // Giả sử active
                .build();
    }

    @Override
    public List<UserResponse> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(user -> UserResponse.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole()) // Lấy role kiểu String
                    .active(true) // Giả sử active
                    .build())
            .collect(Collectors.toList());
    }

    @Override
    public UserProfileResponse getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
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
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }
        return user.getId();
    }

    @Override
    public String getUserRoleByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }
        return user.getRole(); // Trả về role kiểu String
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

        // Chỉ cập nhật các trường có trong request và User model
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());

        // Cập nhật email nếu có và kiểm tra trùng lặp
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                 throw new DuplicateResourceException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật role nếu có trong request (kiểu String)
        if (request.getRole() != null) {
             user.setRole(request.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser);
        return mapToUserProfileResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
         log.info("Creating new user with request: {}", request);
         // Kiểm tra username và email trùng lặp
         if (userRepository.existsByUsername(request.getUsername())) {
             throw new DuplicateResourceException("Username already exists");
         }
         if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
             throw new DuplicateResourceException("Email already exists");
         }

         User user = new User();
         user.setUsername(request.getUsername());
         user.setFirstName(request.getFirstName());
         user.setLastName(request.getLastName());
         user.setEmail(request.getEmail());
         user.setPhoneNumber(request.getPhone());
         user.setPassword(passwordEncoder.encode(request.getPassword()));
         
         // Gán role từ request (String), mặc định là "CUSTOMER" nếu không có
         user.setRole(request.getRole() != null ? request.getRole() : "CUSTOMER");

         User savedUser = userRepository.save(user);
         log.info("User created successfully with ID: {}", savedUser.getId());
         return mapToUserProfileResponse(savedUser);
    }

    @Override
    @Transactional
    public boolean changePassword(String oldPassword, String newPassword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Incorrect old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);
        return true;
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
         log.warn("Attempting to delete user with ID: {}", userId);
         User user = userRepository.findById(userId)
             .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

         userRepository.delete(user);
         log.info("User with ID: {} deleted successfully", userId);
    }

    private UserProfileResponse mapToUserProfileResponse(User user){
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhoneNumber());
        response.setRole(user.getRole()); // Lấy role kiểu String
        response.setEnabled(true); // Giả sử enabled
        response.setRegistrationDate(LocalDate.now()); // Cần lấy ngày đăng ký thực tế từ user nếu có
        return response;
    }
}
