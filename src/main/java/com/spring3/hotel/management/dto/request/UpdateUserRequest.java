package com.spring3.hotel.management.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateUserRequest {
    // private String fullName; // Bỏ fullName
    private String firstName; // Thêm firstName
    private String lastName; // Thêm lastName
    private String email;
    // private String address; // Bỏ nếu không cần
    private String phoneNumber; // Đổi tên phone thành phoneNumber
    // private String gender; // Bỏ nếu không cần
    // private String dateOfBirth; // Bỏ nếu không cần
    // private String nationalId; // Bỏ nếu không cần
    private String role; // Thêm role kiểu String
}
