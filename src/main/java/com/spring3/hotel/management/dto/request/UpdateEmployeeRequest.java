package com.spring3.hotel.management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateEmployeeRequest {
    private String name;
    private String email;
    private String phone;
    private String department; // Sẽ chuyển đổi thành enum Department
    private String position;   // Sẽ chuyển đổi thành enum Position
    private String joinDate;   // Định dạng YYYY-MM-DD
    private Boolean status;
} 
