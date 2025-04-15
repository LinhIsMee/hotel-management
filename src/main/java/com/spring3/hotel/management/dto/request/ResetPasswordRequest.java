package com.spring3.hotel.management.dtos.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
} 