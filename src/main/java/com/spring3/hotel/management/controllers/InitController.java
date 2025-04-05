package com.spring3.hotel.management.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring3.hotel.management.config.FirstTimeInitializer;

@RestController
@RequestMapping("/api/v1/init")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class InitController {

    @Autowired
    private FirstTimeInitializer firstTimeInitializer;
    
    @PostMapping("/dashboard-data")
    public ResponseEntity<String> initDashboardData() {
        try {
            firstTimeInitializer.reinitializeBookingsForDashboard();
            return ResponseEntity.ok("Đã khởi tạo dữ liệu mẫu cho dashboard thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi khởi tạo dữ liệu: " + e.getMessage());
        }
    }
} 