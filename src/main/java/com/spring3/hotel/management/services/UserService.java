package com.spring3.hotel.management.services;


import java.util.List;

import com.spring3.hotel.management.dtos.request.CreateUserRequest;
import com.spring3.hotel.management.dtos.request.RegisterRequest;
import com.spring3.hotel.management.dtos.request.UpdateUserRequest;
import com.spring3.hotel.management.dtos.request.UserRequest;
import com.spring3.hotel.management.dtos.response.UserProfileResponse;
import com.spring3.hotel.management.dtos.response.UserResponse;


public interface UserService {

    UserResponse saveUser(UserRequest userRequest);
    
    UserResponse registerUser(RegisterRequest registerRequest);

    UserResponse getUser();

    List<UserResponse> getAllUser();
    
    UserProfileResponse getUserProfile(Integer id);
    
    UserProfileResponse updateUser(UpdateUserRequest request, Integer id);
    
    UserProfileResponse createUser(CreateUserRequest request);
    
    List<UserProfileResponse> getUserList();

    Integer getUserIdByUsername(String username);

    String getUserRoleByUsername(String username);
    
    boolean processForgotPassword(String email);
    
    boolean resetPassword(String token, String newPassword);
    
    boolean changePassword(String oldPassword, String newPassword);
    
    void deleteUser(Integer userId);
}
