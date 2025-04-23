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
    private String fullName;
    private String email;
    private String address;
    private String phone;
    private String gender;
    private String dateOfBirth;
    private String nationalId;
}
