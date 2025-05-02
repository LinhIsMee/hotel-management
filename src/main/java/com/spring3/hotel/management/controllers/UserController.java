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

import com.spring3.hotel.management.dto.request.AuthRequestDTO;
import com.spring3.hotel.management.dto.request.ChangePasswordRequest;
import com.spring3.hotel.management.dto.request.CreateUserRequest;
import com.spring3.hotel.management.dto.request.ForgotPasswordRequest;
import com.spring3.hotel.management.dto.request.RefreshTokenRequest;
import com.spring3.hotel.management.dto.request.RegisterRequest;
import com.spring3.hotel.management.dto.request.ResetPasswordRequest;
import com.spring3.hotel.management.dto.request.UpdateUserRequest;
import com.spring3.hotel.management.dto.request.UserRequest;
import com.spring3.hotel.management.dto.request.ValidateTokenRequest;
import com.spring3.hotel.management.dto.response.JwtResponseDTO;
import com.spring3.hotel.management.dto.response.MessageResponse;
import com.spring3.hotel.management.dto.response.TokenValidationResponse;
import com.spring3.hotel.management.dto.response.UserProfileResponse;
import com.spring3.hotel.management.dto.response.UserResponse;
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

    /**
     * Đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            UserResponse userResponse = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Lưu thông tin người dùng
     */
    @PostMapping(value = "/save")
    public ResponseEntity saveUser(@RequestBody UserRequest userRequest) {
        try {
            UserResponse userResponse = userService.saveUser(userRequest);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    @PostMapping("/user")
    public ResponseEntity<?> getUserProfile() {
        try {
            UserResponse userResponse = userService.getUser();
            return ResponseEntity.ok().body(userResponse);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi lấy thông tin người dùng: " + e.getMessage()));
        }
    }

    /**
     * API kiểm tra hệ thống
     */
    @GetMapping("/test")
    public String test() {
        try {
            return "Welcome";
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Đăng nhập và tạo token
     */
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

    /**
     * Làm mới token
     */
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

    /**
     * Kiểm tra token có hợp lệ
     */
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody ValidateTokenRequest request) {
        try {
            // Kiểm tra token có được cung cấp không
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Token không được cung cấp"));
            }
            
            String username = jwtService.extractUsername(request.getToken());
            boolean isValid = !jwtService.isTokenExpired(request.getToken());
            
            TokenValidationResponse response = new TokenValidationResponse(isValid, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            // Nếu có lỗi khi xác thực token, trả về token không hợp lệ thay vì lỗi
            return ResponseEntity.ok(new TokenValidationResponse(false, null));
        }
    }

    /**
     * Yêu cầu khôi phục mật khẩu
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            // Kiểm tra email có được cung cấp không
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Email không được để trống"));
            }
            
            boolean result = userService.processForgotPassword(request.getEmail());
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy email"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi xử lý quên mật khẩu: " + e.getMessage()));
        }
    }

    /**
     * Đặt lại mật khẩu
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // Kiểm tra token và mật khẩu mới có được cung cấp không
            if (request.getToken() == null || request.getToken().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Token không được để trống"));
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Mật khẩu mới không được để trống"));
            }
            
            boolean result = userService.resetPassword(request.getToken(), request.getNewPassword());
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Mật khẩu đã được đặt lại thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Token không hợp lệ hoặc đã hết hạn"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi đặt lại mật khẩu: " + e.getMessage()));
        }
    }

    /**
     * Đăng xuất khỏi hệ thống
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        try {
            // Lấy thông tin người dùng hiện tại từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Kiểm tra xem có authentication không
            if (authentication == null || authentication.getName() == null || 
                "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.ok(new MessageResponse("Không có phiên đăng nhập nào đang hoạt động"));
            }
            
            String username = authentication.getName();

            // Xóa refresh token của người dùng khỏi cơ sở dữ liệu
            refreshTokenService.deleteByUsername(username);

            return ResponseEntity.ok(new MessageResponse("Đăng xuất thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Đăng xuất thất bại: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin chi tiết của người dùng theo ID
     */
    @GetMapping("/user/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Integer userId) {
        try {
            UserProfileResponse userResponse = userService.getUserProfile(userId);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi lấy thông tin người dùng: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin người dùng
     */
    @PutMapping("/user/update/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request, @PathVariable Integer userId) {
        try {
            UserProfileResponse userResponse = userService.updateUser(request, userId);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi cập nhật thông tin người dùng: " + e.getMessage()));
        }
    }

    /**
     * Tạo người dùng mới
     */
    @PostMapping("/user/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserProfileResponse userResponse = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi tạo người dùng mới: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả người dùng
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUserList() {
        try {
            List<UserProfileResponse> userResponses = userService.getUserList();
            return ResponseEntity.ok(userResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi lấy danh sách người dùng: " + e.getMessage()));
        }
    }

    /**
     * Xóa người dùng theo ID
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(new MessageResponse("Xóa người dùng thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi xóa người dùng: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin cá nhân của người dùng đang đăng nhập
     */
    @GetMapping("/user/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // Lấy thông tin người dùng hiện tại từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Lấy ID người dùng từ username
            Integer userId = userService.getUserIdByUsername(username);
            
            // Lấy thông tin đầy đủ của người dùng
            UserProfileResponse userResponse = userService.getUserProfile(userId);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi lấy thông tin cá nhân: " + e.getMessage()));
        }
    }
    
    /**
     * Thay đổi mật khẩu
     */
    @PostMapping("/user/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Mật khẩu cũ không được để trống"));
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Mật khẩu mới không được để trống"));
            }
            
            // Thực hiện đổi mật khẩu
            boolean result = userService.changePassword(request.getOldPassword(), request.getNewPassword());
            
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Đổi mật khẩu thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Mật khẩu cũ không chính xác"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi đổi mật khẩu: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin người dùng theo ID
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId) {
        try {
            UserProfileResponse userResponse = userService.getUserProfile(userId);
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy người dùng với ID: " + userId));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi lấy thông tin người dùng: " + errorMessage));
        }
    }
}
