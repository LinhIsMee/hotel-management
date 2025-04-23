package com.spring3.hotel.management.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring3.hotel.management.dto.response.EmployeeResponse;
import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Position;
import com.spring3.hotel.management.services.EmployeeService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * Hiển thị trang quản lý nhân viên
     */
    @GetMapping("/employees")
    public String employeesPage(Model model) {
        try {
            List<EmployeeResponse> employees = employeeService.getAllEmployees();
            model.addAttribute("employees", employees);
            model.addAttribute("departments", Department.values());
            model.addAttribute("positions", Position.values());
            return "admin/employees";
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách nhân viên: {}", e.getMessage());
            model.addAttribute("error", "Không thể lấy danh sách nhân viên. Vui lòng thử lại sau.");
            return "admin/error";
        }
    }
} 
