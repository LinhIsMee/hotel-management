package com.spring3.hotel.management.services;


import com.spring3.hotel.management.dtos.request.CreateUserRequest;
import com.spring3.hotel.management.dtos.request.UpdateUserRequest;
import com.spring3.hotel.management.dtos.request.UserRequest;
import com.spring3.hotel.management.dtos.response.UserProfileResponse;
import com.spring3.hotel.management.dtos.response.UserResponse;
import com.spring3.hotel.management.models.Role;

import java.util.List;


public interface UserService {

    UserResponse saveUser(UserRequest userRequest);

    UserResponse getUser();

    List<UserResponse> getAllUser();
    public UserProfileResponse getUserProfile(Integer id);
    UserProfileResponse updateUser(UpdateUserRequest request, Integer id);
    UserProfileResponse createUser(CreateUserRequest request);
    List<UserProfileResponse> getUserList();

    Integer getUserIdByUsername(String username);

    String getUserRoleByUsername(String username);
}
