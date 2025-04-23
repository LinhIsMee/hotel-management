package com.spring3.hotel.management.services;

import java.util.List;

import com.spring3.hotel.management.dto.request.CreateEmployeeRequest;
import com.spring3.hotel.management.dto.request.UpdateEmployeeRequest;
import com.spring3.hotel.management.dto.response.EmployeeResponse;
import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Position;

public interface EmployeeService {
    List<EmployeeResponse> getAllEmployees();
    EmployeeResponse getEmployeeById(Integer id);
    EmployeeResponse createEmployee(CreateEmployeeRequest request);
    EmployeeResponse updateEmployee(Integer id, UpdateEmployeeRequest request);
    void deleteEmployee(Integer id);
    
    // Các phương thức lọc
    List<EmployeeResponse> getEmployeesByDepartment(Department department);
    List<EmployeeResponse> getEmployeesByPosition(Position position);
    List<EmployeeResponse> getEmployeesByStatus(Boolean status);
} 
