package com.spring3.hotel.management.dtos.request;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
} 