package com.spring3.hotel.management.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring3.hotel.management.dtos.request.CreateEmployeeRequest;
import com.spring3.hotel.management.dtos.request.UpdateEmployeeRequest;
import com.spring3.hotel.management.dtos.response.EmployeeResponse;
import com.spring3.hotel.management.dtos.response.MessageResponse;
import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Position;
import com.spring3.hotel.management.services.EmployeeService;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    
    @GetMapping
    public ResponseEntity<?> getAllEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Boolean status) {
        try {
            List<EmployeeResponse> employees;
            
            if (department != null) {
                // Lọc theo phòng ban
                try {
                    Department departmentEnum = Department.valueOf(department);
                    employees = employeeService.getEmployeesByDepartment(departmentEnum);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Invalid department: " + department));
                }
            } else if (position != null) {
                // Lọc theo vị trí
                try {
                    Position positionEnum = Position.valueOf(position);
                    employees = employeeService.getEmployeesByPosition(positionEnum);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Invalid position: " + position));
                }
            } else if (status != null) {
                // Lọc theo trạng thái
                employees = employeeService.getEmployeesByStatus(status);
            } else {
                // Lấy tất cả
                employees = employeeService.getAllEmployees();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", employees);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi lấy danh sách nhân viên: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{employeeId}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Integer employeeId) {
        try {
            EmployeeResponse employee = employeeService.getEmployeeById(employeeId);
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy nhân viên với ID: " + employeeId));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi lấy thông tin nhân viên: " + errorMessage));
        }
    }
    
    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody CreateEmployeeRequest request) {
        try {
            EmployeeResponse createdEmployee = employeeService.createEmployee(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("already exists")) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse(errorMessage));
            }
            if (errorMessage != null && errorMessage.contains("Invalid")) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse(errorMessage));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi tạo nhân viên mới: " + errorMessage));
        }
    }
    
    @PutMapping("/{employeeId}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Integer employeeId,
            @RequestBody UpdateEmployeeRequest request) {
        try {
            EmployeeResponse updatedEmployee = employeeService.updateEmployee(employeeId, request);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy nhân viên với ID: " + employeeId));
            }
            if (errorMessage != null && errorMessage.contains("already exists")) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse(errorMessage));
            }
            if (errorMessage != null && errorMessage.contains("Invalid")) {
                return ResponseEntity.badRequest()
                    .body(new MessageResponse(errorMessage));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi cập nhật thông tin nhân viên: " + errorMessage));
        }
    }
    
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Integer employeeId) {
        try {
            employeeService.deleteEmployee(employeeId);
            return ResponseEntity.ok(new MessageResponse("Xóa nhân viên thành công"));
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Không tìm thấy nhân viên với ID: " + employeeId));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Lỗi khi xóa nhân viên: " + errorMessage));
        }
    }
} 