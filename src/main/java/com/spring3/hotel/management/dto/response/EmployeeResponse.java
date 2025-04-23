package com.spring3.hotel.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmployeeResponse {
    private Integer id;
    private String name;
    private String email;
    private String phone;
    private String department;
    private String position;
    private String joinDate;
    private Boolean status;
} 
