package com.spring3.hotel.management.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring3.hotel.management.dto.request.AuthRequestDTO;
import com.spring3.hotel.management.dto.request.ChangePasswordRequest;
import com.spring3.hotel.management.dto.request.CreateUserRequest;
import com.spring3.hotel.management.dto.request.ForgotPasswordRequest;
import com.spring3.hotel.management.dto.request.RegisterRequest;
import com.spring3.hotel.management.dto.request.ResetPasswordRequest;
import com.spring3.hotel.management.dto.request.UpdateUserRequest;
import com.spring3.hotel.management.dto.request.ValidateTokenRequest;
import com.spring3.hotel.management.dto.response.JwtResponseDTO;
import com.spring3.hotel.management.dto.response.MessageResponse;
import com.spring3.hotel.management.dto.response.SuccessResponse;
import com.spring3.hotel.management.dto.response.TokenValidationResponse;
import com.spring3.hotel.management.dto.response.UserProfileResponse;
import com.spring3.hotel.management.dto.response.UserResponse;
import com.spring3.hotel.management.services.JwtService;
import com.spring3.hotel.management.services.UserService;


@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    /**
     * Đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (registerRequest.getUsername() == null || registerRequest.getUsername().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Tên đăng nhập không được để trống"));
            }
            
            if (registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Mật khẩu không được để trống"));
            }
            
            if (registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Email không được để trống"));
            }
            
            UserResponse userResponse = userService.registerUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(HttpStatus.CREATED.value(), "Đăng ký tài khoản thành công", userResponse));
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && (errorMessage.contains("Username already exists") || 
                                        errorMessage.contains("Email already exists"))) {
                return ResponseEntity.badRequest().body(new MessageResponse(errorMessage));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi đăng ký tài khoản: " + errorMessage));
        }
    }

    // API đã được loại bỏ, sử dụng POST /user/create hoặc PUT /user/update/{userId} thay thế
    // API đã được loại bỏ, sử dụng GET /user/profile thay thế

    /**
     * Đăng nhập và tạo token
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO) {
        try {
            // Kiểm tra dữ liệu đầu vào
            if (authRequestDTO.getUsername() == null || authRequestDTO.getUsername().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Tên đăng nhập không được để trống"));
            }
            
            if (authRequestDTO.getPassword() == null || authRequestDTO.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Mật khẩu không được để trống"));
            }
            
            // Xác thực người dùng
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword())
            );

            if (authentication.isAuthenticated()) {
                // Tạo mới refresh token
                String accessToken = jwtService.GenerateToken(authRequestDTO.getUsername());
                Integer userId = userService.getUserIdByUsername(authRequestDTO.getUsername());
                String role = userService.getUserRoleByUsername(authRequestDTO.getUsername());

                JwtResponseDTO response = JwtResponseDTO.builder()
                    .accessToken(accessToken)
                    .userId(userId)
                    .role(role)
                    .build();
                    
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Thông tin đăng nhập không hợp lệ"));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Bad credentials")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Tên đăng nhập hoặc mật khẩu không chính xác"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi đăng nhập: " + errorMessage));
        }
    }

    /**
     * API xác thực token - gộp các chức năng liên quan đến token
     * Hỗ trợ các thao tác: refresh (làm mới token) và validate (kiểm tra token)
     * 
     * @param action Hành động cần thực hiện (refresh hoặc validate)
     * @param request Dữ liệu yêu cầu (RefreshTokenRequest hoặc ValidateTokenRequest)
     * @return Kết quả xác thực token
     */
    @PostMapping("/auth/{action}")
    public ResponseEntity<?> handleTokenOperations(
            @PathVariable String action,
            @RequestBody Object request) {
        try {
            // Xử lý làm mới token
            if ("refresh".equals(action)) {
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(new MessageResponse("Chức năng refresh token chưa được triển khai"));
            }
            
            // Xử lý kiểm tra token
            else if ("validate".equals(action)) {
                if (!(request instanceof ValidateTokenRequest)) {
                    return ResponseEntity.badRequest()
                        .body(new MessageResponse("Dữ liệu không hợp lệ cho hành động validate"));
                }
                
                ValidateTokenRequest validateRequest = (ValidateTokenRequest) request;
                
                // Kiểm tra token có được cung cấp không
                if (validateRequest.getToken() == null || validateRequest.getToken().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Token không được cung cấp"));
                }
                
                String username = jwtService.extractUsername(validateRequest.getToken());
                boolean isValid = !jwtService.isTokenExpired(validateRequest.getToken());
                
                TokenValidationResponse response = new TokenValidationResponse(isValid, username);
                return ResponseEntity.ok(response);
            }
            
            // Hành động không hợp lệ
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Hành động không hợp lệ. Hỗ trợ: refresh, validate"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Đã xảy ra lỗi khi xử lý token: " + e.getMessage()));
        }
    }
    
    // API đã được loại bỏ, sử dụng POST /auth/refresh thay thế
    
    // API đã được loại bỏ, sử dụng POST /auth/validate thay thế

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
            
            // boolean result = userService.processForgotPassword(request.getEmail()); // Commenting out: Undefined method
            boolean result = false; // Temporary placeholder
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse("Không thể xử lý yêu cầu quên mật khẩu. Vui lòng thử lại."));
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
            
            // boolean result = userService.resetPassword(request.getToken(), request.getNewPassword()); // Commenting out: Undefined method
            boolean result = false; // Temporary placeholder
            if (result) {
                return ResponseEntity.ok(new MessageResponse("Mật khẩu đã được đặt lại thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Token không hợp lệ, đã hết hạn hoặc không thể đặt lại mật khẩu."));
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

            return ResponseEntity.ok(new MessageResponse("Đăng xuất thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Đăng xuất thất bại: " + e.getMessage()));
        }
    }

    // Các API trùng lặp đã được gộp vào endpoint /users/{userId} và /user/profile/{userId}

    /**
     * Cập nhật thông tin người dùng
     */
    @PutMapping("/user/update/{userId}")
    public ResponseEntity<?> updateUser(@RequestBody UpdateUserRequest request, @PathVariable Integer userId) {
        try {
            UserProfileResponse userResponse = userService.updateUser(request, userId);
            return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Cập nhật thông tin người dùng thành công", userResponse)
            );
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
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<>(HttpStatus.CREATED.value(), "Tạo người dùng mới thành công", userResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi tạo người dùng mới: " + e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả người dùng với khả năng lọc theo vai trò
     */
    @GetMapping("/users")
    public ResponseEntity<?> getUserList(@RequestParam(required = false) String role) {
        try {
            List<UserProfileResponse> userResponses;
            String message = "Lấy danh sách người dùng thành công";
            
            if (role != null && !role.isEmpty()) {
                // Lọc người dùng theo vai trò
                userResponses = userService.getUserList().stream()
                    .filter(user -> user.getRole().equalsIgnoreCase(role))
                    .collect(Collectors.toList());
                message = "Lấy danh sách người dùng theo vai trò thành công";
            } else {
                // Lấy tất cả người dùng
                userResponses = userService.getUserList();
            }
            
            return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), message, userResponses)
            );
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
            return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Xóa người dùng thành công", null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi xóa người dùng: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin cá nhân của người dùng đang đăng nhập
     * API này thay thế cho các API trùng lặp về thông tin người dùng
     */
    @GetMapping("/user/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            // Lấy thông tin người dùng hiện tại từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Kiểm tra xem có phải anonymousUser không
            if ("anonymousUser".equals(username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Người dùng chưa đăng nhập"));
            }
            
            // Lấy ID người dùng từ username
            Integer userId = userService.getUserIdByUsername(username);
            
            // Lấy thông tin đầy đủ của người dùng
            UserProfileResponse userResponse = userService.getUserProfile(userId);
            return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin cá nhân thành công", userResponse)
            );
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
            
            // Kiểm tra độ dài và độ phức tạp của mật khẩu mới
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body(new MessageResponse("Mật khẩu mới phải có ít nhất 6 ký tự"));
            }
            
            // Thực hiện đổi mật khẩu
            boolean result = userService.changePassword(request.getOldPassword(), request.getNewPassword());
            
            if (result) {
                return ResponseEntity.ok(
                    new SuccessResponse<>(HttpStatus.OK.value(), "Đổi mật khẩu thành công", null)
                );
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
     * Endpoint này thay thế cho cả /user/profile/{userId} và /users/{userId}
     */
    @GetMapping({"/user/profile/{userId}", "/users/{userId}"})
    public ResponseEntity<?> getUserById(@PathVariable Integer userId) {
        try {
            UserProfileResponse userResponse = userService.getUserProfile(userId);
            return ResponseEntity.ok(
                new SuccessResponse<>(HttpStatus.OK.value(), "Lấy thông tin người dùng thành công", userResponse)
            );
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
