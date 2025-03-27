package com.spring3.hotel.management.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserProfileResponse {
    private Integer id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String gender;
    private String dateOfBirth;
    private String nationalId;
    private String createdAt;
    private String updatedAt;
}
