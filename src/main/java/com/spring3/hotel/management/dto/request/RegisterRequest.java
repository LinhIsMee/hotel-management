package com.spring3.hotel.management.dtos.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String gender;
    private String dateOfBirth;
    private String address;
    private String nationalId;
} 