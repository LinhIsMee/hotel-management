package com.spring3.hotel.management.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring3.hotel.management.dtos.request.AuthRequestDTO;
import com.spring3.hotel.management.dtos.request.CreateUserRequest;
import com.spring3.hotel.management.dtos.request.ForgotPasswordRequest;
import com.spring3.hotel.management.dtos.request.RefreshTokenRequest;
import com.spring3.hotel.management.dtos.request.RegisterRequest;
import com.spring3.hotel.management.dtos.request.ResetPasswordRequest;
import com.spring3.hotel.management.dtos.request.UpdateUserRequest;
import com.spring3.hotel.management.dtos.request.UserRequest;
import com.spring3.hotel.management.dtos.request.ValidateTokenRequest;
import com.spring3.hotel.management.dtos.response.JwtResponseDTO;
import com.spring3.hotel.management.dtos.response.MessageResponse;
import com.spring3.hotel.management.dtos.response.TokenValidationResponse;
import com.spring3.hotel.management.dtos.response.UserProfileResponse;
import com.spring3.hotel.management.dtos.response.UserResponse;
import com.spring3.hotel.management.models.RefreshToken;
import com.spring3.hotel.management.services.JwtService;
import com.spring3.hotel.management.services.RefreshTokenService;
import com.spring3.hotel.management.services.UserService;


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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            UserResponse userResponse = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

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

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            // Kiểm tra token có được cung cấp không
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Refresh token không được cung cấp"));
            }
            
            return refreshTokenService.findByToken(request.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    // Tạo token mới
                    String accessToken = jwtService.GenerateToken(user.getUsername());
                    Integer userId = user.getId();
                    String role = user.getRole().getName();
                    
                    return ResponseEntity.ok(JwtResponseDTO.builder()
                        .accessToken(accessToken)
                        .userId(userId)
                        .role(role)
                        .token(request.getToken())
                        .build());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại trong cơ sở dữ liệu"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Đã xảy ra lỗi khi làm mới token: " + e.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequest request) {
        try {
            String username = jwtService.extractUsername(request.getToken());
            boolean isValid = !jwtService.isTokenExpired(request.getToken());
            
            TokenValidationResponse response = new TokenValidationResponse(isValid, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(new TokenValidationResponse(false, null));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            boolean result = userService.processForgotPassword(request.getEmail());
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Password reset instructions sent to your email"));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Email not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            boolean result = userService.resetPassword(request.getToken(), request.getNewPassword());
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Password has been reset successfully"));
            } else {
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
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

            return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
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
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        UserProfileResponse userResponse = userService.createUser(request);
        return new ResponseEntity<>(userResponse, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_STAFF')")
    public ResponseEntity<?> getUserList() {
        try {
            List<UserProfileResponse> userResponses = userService.getUserList();
            return ResponseEntity.ok(userResponses);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
