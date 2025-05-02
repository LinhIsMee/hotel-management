package com.spring3.hotel.management.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String phone;
    private String gender;
    private String dateOfBirth;
    private String address;
    private String nationalId;
    
    public void setPhone(String phone) {
        this.phone = phone;
        this.phoneNumber = phone;
    }
    
    public String getPhone() {
        return phone != null ? phone : phoneNumber;
    }
} 