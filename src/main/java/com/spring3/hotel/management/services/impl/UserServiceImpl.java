package com.spring3.hotel.management.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

import com.spring3.hotel.management.dtos.request.CreateUserRequest;
import com.spring3.hotel.management.dtos.request.RegisterRequest;
import com.spring3.hotel.management.dtos.request.UpdateUserRequest;
import com.spring3.hotel.management.dtos.request.UserRequest;
import com.spring3.hotel.management.dtos.response.UserProfileResponse;
import com.spring3.hotel.management.dtos.response.UserResponse;
import com.spring3.hotel.management.exceptions.BadRequestException;
import com.spring3.hotel.management.exceptions.NotFoundException;
import com.spring3.hotel.management.models.PasswordResetToken;
import com.spring3.hotel.management.models.Role;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.repositories.PasswordResetTokenRepository;
import com.spring3.hotel.management.repositories.RoleRepository;
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
    
    @Autowired
    RoleRepository roleRepository;
    
    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;
    
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
        user.setFullName(registerRequest.getFullName());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setGender(registerRequest.getGender());
        
        if (registerRequest.getDateOfBirth() != null && !registerRequest.getDateOfBirth().isEmpty()) {
            user.setDateOfBirth(LocalDate.parse(registerRequest.getDateOfBirth()));
        }
        
        user.setAddress(registerRequest.getAddress());
        user.setNationalId(registerRequest.getNationalId());
        user.setCreatedAt(LocalDateTime.now());
        
        // Set default role to USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
        user.setRole(userRole);
        
        User savedUser = userRepository.save(user);
        
        // Tạo response đúng cách
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setFullName(savedUser.getFullName());
        response.setRole("ROLE_USER");
        
        return response;
    }

    @Override
    public UserResponse saveUser(UserRequest userRequest) {
        if(userRequest.getUsername() == null){
            throw new RuntimeException("Parameter username is not found in request..!!");
        } else if(userRequest.getPassword() == null){
            throw new RuntimeException("Parameter password is not found in request..!!");
        }

        User savedUser = null;

        User user = modelMapper.map(userRequest, User.class);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        
        // Thiết lập vai trò mặc định là ROLE_USER nếu không có role trong request
        if (user.getRole() == null) {
            Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
            user.setRole(userRole);
        }
        
        if(userRequest.getId() != null){
            User oldUser = userRepository.findFirstById(userRequest.getId());
            if(oldUser != null){
                oldUser.setId(user.getId());
                oldUser.setPassword(user.getPassword());
                oldUser.setUsername(user.getUsername());
                oldUser.setEmail(user.getEmail());
                oldUser.setFullName(user.getFullName());
                
                // Cập nhật role nếu có
                if (user.getRole() != null) {
                    oldUser.setRole(user.getRole());
                } else {
                    // Giữ nguyên role cũ nếu không có role mới
                    if (oldUser.getRole() == null) {
                        Role userRole = roleRepository.findByName("ROLE_USER")
                            .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));
                        oldUser.setRole(userRole);
                    }
                }
                
                oldUser.setUpdatedAt(LocalDateTime.now());
                savedUser = userRepository.save(oldUser);
            } else {
                throw new RuntimeException("Can't find record with identifier: " + userRequest.getId());
            }
        } else {
            // Thiết lập ngày tạo cho người dùng mới
            user.setCreatedAt(LocalDateTime.now());
            savedUser = userRepository.save(user);
        }
        
        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setFullName(savedUser.getFullName());
        
        if (savedUser.getRole() != null) {
            response.setRole(savedUser.getRole().getName());
        }
        
        return response;
    }

    @Override
    public UserResponse getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String usernameFromAccessToken = userDetail.getUsername();
        User user = userRepository.findByUsername(usernameFromAccessToken);
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        
        if (user.getRole() != null) {
            response.setRole(user.getRole().getName());
        }
        
        return response;
    }

    @Override
    public List<UserResponse> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream()
            .map(user -> {
                UserResponse response = new UserResponse();
                response.setId(user.getId());
                response.setUsername(user.getUsername());
                response.setEmail(user.getEmail());
                response.setFullName(user.getFullName());
                
                if (user.getRole() != null) {
                    response.setRole(user.getRole().getName());
                }
                
                return response;
            })
            .collect(Collectors.toList());
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
        User user = modelMapper.map(request, User.class);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(request.getRole());

        log.info("Creating user: {}", user);
        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser);

        return mapToUserProfileResponse(savedUser);
    }
    
    @Override
    @Transactional
    public boolean processForgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }
        
        User user = userOptional.get();
        
        // Xóa token cũ nếu có
        passwordResetTokenRepository.findByUser(user).ifPresent(token -> {
            passwordResetTokenRepository.delete(token);
        });
        
        // Tạo token mới
        PasswordResetToken passwordResetToken = new PasswordResetToken(user);
        passwordResetTokenRepository.save(passwordResetToken);
        
        // Gửi email nếu có EmailService
        if (emailService != null) {
            String resetUrl = "http://localhost:3000/reset-password?token=" + passwordResetToken.getToken();
            String emailContent = "Để đặt lại mật khẩu, vui lòng nhấp vào liên kết sau: " + resetUrl;
            
            try {
                emailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", emailContent);
            } catch (Exception e) {
                log.error("Error sending reset password email", e);
                // Không throw exception để không ảnh hưởng đến luồng chính
            }
        } else {
            log.warn("EmailService is not configured. Reset password email not sent.");
        }
        
        return true;
    }
    
    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
        if (tokenOptional.isEmpty()) {
            return false;
        }
        
        PasswordResetToken passwordResetToken = tokenOptional.get();
        
        if (passwordResetToken.isExpired()) {
            // Xóa token hết hạn
            passwordResetTokenRepository.delete(passwordResetToken);
            return false;
        }
        
        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        // Xóa token sau khi sử dụng
        passwordResetTokenRepository.delete(passwordResetToken);
        
        return true;
    }
    
    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        // Xóa token reset mật khẩu nếu có
        passwordResetTokenRepository.findByUser(user).ifPresent(token -> {
            passwordResetTokenRepository.delete(token);
        });
        
        // Xóa người dùng
        userRepository.delete(user);
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
        if (user.getRole() != null) {
            userProfileResponse.setRole(user.getRole().getName());
        }
        return userProfileResponse;
    }

}
