package com.spring3.hotel.management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring3.hotel.management.models.Department;
import com.spring3.hotel.management.models.Employee;
import com.spring3.hotel.management.models.Position;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByDepartment(Department department);
    List<Employee> findByPosition(Position position);
    List<Employee> findByStatus(Boolean status);
} 