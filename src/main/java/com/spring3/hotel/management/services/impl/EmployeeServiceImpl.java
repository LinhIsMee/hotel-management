package com.spring3.hotel.management.services.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring3.hotel.management.dtos.request.CreateEmployeeRequest;
import com.spring3.hotel.management.dtos.request.UpdateEmployeeRequest;
import com.spring3.hotel.management.dtos.response.EmployeeResponse;
import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Employee;
import com.spring3.hotel.management.models.Position;
import com.spring3.hotel.management.repositories.EmployeeRepository;
import com.spring3.hotel.management.services.EmployeeService;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse getEmployeeById(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return mapToEmployeeResponse(employee);
    }

    @Override
    public List<EmployeeResponse> getEmployeesByDepartment(Department department) {
        List<Employee> employees = employeeRepository.findByDepartment(department);
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByPosition(Position position) {
        List<Employee> employees = employeeRepository.findByPosition(position);
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeResponse> getEmployeesByStatus(Boolean status) {
        List<Employee> employees = employeeRepository.findByStatus(status);
        return employees.stream()
                .map(this::mapToEmployeeResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Kiểm tra số điện thoại đã tồn tại chưa
        if (employeeRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists: " + request.getPhone());
        }
        
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        
        try {
            employee.setDepartment(Department.valueOf(request.getDepartment()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid department: " + request.getDepartment());
        }
        
        try {
            employee.setPosition(Position.valueOf(request.getPosition()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid position: " + request.getPosition());
        }
        
        try {
            employee.setJoinDate(LocalDate.parse(request.getJoinDate()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid join date format: " + request.getJoinDate());
        }
        
        employee.setStatus(true); // Mặc định là active
        
        employee = employeeRepository.save(employee);
        
        return mapToEmployeeResponse(employee);
    }

    @Override
    public EmployeeResponse updateEmployee(Integer id, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Kiểm tra email đã tồn tại chưa (nếu email thay đổi)
        if (!employee.getEmail().equals(request.getEmail()) &&
                employeeRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        
        // Kiểm tra số điện thoại đã tồn tại chưa (nếu số điện thoại thay đổi)
        if (!employee.getPhone().equals(request.getPhone()) &&
                employeeRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists: " + request.getPhone());
        }
        
            employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        
            try {
            employee.setDepartment(Department.valueOf(request.getDepartment()));
            } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid department: " + request.getDepartment());
        }
        
            try {
            employee.setPosition(Position.valueOf(request.getPosition()));
            } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid position: " + request.getPosition());
            }
        
        try {
            employee.setJoinDate(LocalDate.parse(request.getJoinDate()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid join date format: " + request.getJoinDate());
        }
        
            employee.setStatus(request.getStatus());
        
        employee = employeeRepository.save(employee);
        
        return mapToEmployeeResponse(employee);
    }

    @Override
    public void deleteEmployee(Integer id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Có thể thực hiện xóa mềm bằng cách đặt status = false
        employee.setStatus(false);
        
        employeeRepository.save(employee);
    }
    
    private EmployeeResponse mapToEmployeeResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setName(employee.getName());
        response.setEmail(employee.getEmail());
        response.setPhone(employee.getPhone());
        response.setDepartment(employee.getDepartment().toString());
        response.setPosition(employee.getPosition().toString());
        response.setJoinDate(employee.getJoinDate().toString());
        response.setStatus(employee.getStatus());
        return response;
    }
} 