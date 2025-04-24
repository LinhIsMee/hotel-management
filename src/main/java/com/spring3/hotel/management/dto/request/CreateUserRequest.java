package com.spring3.hotel.management.dto.request;

// Bỏ import Role không dùng nữa
// import com.spring3.hotel.management.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateUserRequest {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    // Bỏ các trường không dùng
    // private String address;
    private String phone;
    // private String gender;
    // private String dateOfBirth;
    // private String nationalId;
    private String password;
    // Đổi Role thành String
    private String role;
}
