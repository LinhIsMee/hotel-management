package com.spring3.hotel.management.services.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.spring3.hotel.management.dtos.request.CreateEmployeeRequest;
import com.spring3.hotel.management.dtos.request.UpdateEmployeeRequest;
import com.spring3.hotel.management.dtos.response.EmployeeResponse;
import com.spring3.hotel.management.exceptions.BadRequestException;
import com.spring3.hotel.management.exceptions.DuplicateResourceException;
import com.spring3.hotel.management.exceptions.NotFoundException;
import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Employee;
import com.spring3.hotel.management.models.Position;
import com.spring3.hotel.management.repositories.EmployeeRepository;
import com.spring3.hotel.management.services.EmployeeService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse getEmployeeById(Integer id) {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
        
        return mapToEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        // Kiểm tra email đã tồn tại chưa
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (request.getPhone() != null && employeeRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone number already exists");
        }
        
        // Tạo nhân viên mới
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        
        // Xử lý Department
        try {
            Department department = Department.valueOf(request.getDepartment());
            employee.setDepartment(department);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid department: " + request.getDepartment());
        }
        
        // Xử lý Position
        try {
            Position position = Position.valueOf(request.getPosition());
            employee.setPosition(position);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid position: " + request.getPosition());
        }
        
        // Xử lý ngày tham gia
        try {
            LocalDate joinDate = LocalDate.parse(request.getJoinDate());
            employee.setJoinDate(joinDate);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid join date format. Please use YYYY-MM-DD format");
        }
        
        // Xử lý trạng thái
        employee.setStatus(request.getStatus() != null ? request.getStatus() : true);
        
        // Lưu nhân viên
        Employee savedEmployee = employeeRepository.save(employee);
        
        return mapToEmployeeResponse(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(Integer id, UpdateEmployeeRequest request) {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        // Tìm nhân viên cần cập nhật
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
        
        // Kiểm tra email đã tồn tại chưa (nếu email thay đổi)
        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())) {
            if (employeeRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
            employee.setEmail(request.getEmail());
        }
        
        // Kiểm tra số điện thoại đã tồn tại chưa (nếu số điện thoại thay đổi)
        if (request.getPhone() != null && !request.getPhone().equals(employee.getPhone())) {
            if (employeeRepository.existsByPhone(request.getPhone())) {
                throw new DuplicateResourceException("Phone number already exists");
            }
            employee.setPhone(request.getPhone());
        }
        
        // Cập nhật thông tin khác
        if (request.getName() != null) {
            employee.setName(request.getName());
        }
        
        // Xử lý Department
        if (request.getDepartment() != null) {
            try {
                Department department = Department.valueOf(request.getDepartment());
                employee.setDepartment(department);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid department: " + request.getDepartment());
            }
        }
        
        // Xử lý Position
        if (request.getPosition() != null) {
            try {
                Position position = Position.valueOf(request.getPosition());
                employee.setPosition(position);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid position: " + request.getPosition());
            }
        }
        
        // Xử lý ngày tham gia
        if (request.getJoinDate() != null) {
            try {
                LocalDate joinDate = LocalDate.parse(request.getJoinDate());
                employee.setJoinDate(joinDate);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid join date format. Please use YYYY-MM-DD format");
            }
        }
        
        // Xử lý trạng thái
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        
        // Lưu thay đổi
        Employee updatedEmployee = employeeRepository.save(employee);
        
        return mapToEmployeeResponse(updatedEmployee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Integer id) {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        // Tìm nhân viên cần xóa
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
        
        // Xóa nhân viên
        employeeRepository.delete(employee);
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(Department department) {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        List<Employee> employees = employeeRepository.findByDepartment(department);
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByPosition(Position position) {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        List<Employee> employees = employeeRepository.findByPosition(position);
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByStatus(Boolean status) {
        // Kiểm tra quyền admin
        checkAdminRole();
        
        List<Employee> employees = employeeRepository.findByStatus(status);
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    
    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setName(employee.getName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setDepartment(employee.getDepartment().name());
        response.setPosition(employee.getPosition().name());
        response.setJoinDate(employee.getJoinDate().toString());
        response.setStatus(employee.getStatus());
        return response;
    }
    
    private void checkAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Chưa đăng nhập");
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin) {
            throw new AccessDeniedException("Access Denied: Chỉ admin mới có quyền thực hiện thao tác này");
        }
    }
} 