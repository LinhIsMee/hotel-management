package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dtos.request.*;
import com.spring3.hotel.management.dtos.response.JwtResponseDTO;
import com.spring3.hotel.management.dtos.response.UserProfileResponse;
import com.spring3.hotel.management.dtos.response.UserResponse;
import com.spring3.hotel.management.models.RefreshToken;
import com.spring3.hotel.management.services.JwtService;
import com.spring3.hotel.management.services.RefreshTokenService;
import com.spring3.hotel.management.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    RefreshTokenService refreshTokenService;


    @Autowired
    private  AuthenticationManager authenticationManager;

    @PostMapping(value = "/save")
    public ResponseEntity saveUser(@RequestBody UserRequest userRequest) {
        try {
            UserResponse userResponse = userService.saveUser(userRequest);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/user")
    public ResponseEntity<UserResponse> getUserProfile() {
        try {
        UserResponse userResponse = userService.getUser();
        return ResponseEntity.ok().body(userResponse);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/test")
    public String test() {
        try {
            return "Welcome";
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public JwtResponseDTO AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO) {
        // Xác thực người dùng
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
        );

        if (authentication.isAuthenticated()) {
            // Xóa refresh token cũ (nếu có)
            refreshTokenService.deleteByUsername(authRequestDTO.getUsername()); // Xóa refresh token cũ (nếu có)

            // Tạo mới refresh token
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            String accessToken = jwtService.GenerateToken(authRequestDTO.getUsername());
            Integer userId = userService.getUserIdByUsername(authRequestDTO.getUsername());
            String role = userService.getUserRoleByUsername(authRequestDTO.getUsername());

            return JwtResponseDTO.builder()
                .accessToken(accessToken)
                .userId(userId)
                .role(role)
                .token(newRefreshToken.getToken())
                .build();
        } else {
            throw new UsernameNotFoundException("Invalid user request..!!");
        }
    }

        @PostMapping("/logout")
        public ResponseEntity<?> logoutUser() {
            try {
                // Lấy thông tin người dùng hiện tại từ SecurityContext
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();

                // Xóa refresh token của người dùng khỏi cơ sở dữ liệu
                refreshTokenService.deleteByUsername(username);

                return ResponseEntity.ok("Logged out successfully");
            } catch (Exception e) {
                throw new RuntimeException("Logout failed: " + e.getMessage());
            }
        }

    @GetMapping("/user/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Integer userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/user/update/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request, @PathVariable Integer userId) {
        UserProfileResponse userResponse = userService.updateUser(request, userId);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @PostMapping("/user/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        UserProfileResponse userResponse = userService.createUser(request);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUserList() {
        try {
            List<UserProfileResponse> userResponses = userService.getUserList();
            return ResponseEntity.ok(userResponses);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
