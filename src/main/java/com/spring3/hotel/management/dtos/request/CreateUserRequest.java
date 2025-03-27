package com.spring3.hotel.management.dtos.request;

import com.spring3.hotel.management.models.Role;
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
    private String fullName;
    private String email;
    private String address;
    private String phone;
    private String gender;
    private String dateOfBirth;
    private String nationalId;
    private String password;
    private Role role;
}
