package com.spring3.hotel.management.dto.request;

import com.spring3.hotel.management.models.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserRequest {

    private Integer id;
    private String username;
    private String password;
    private Role role;
    private String email;
    private String fullName;

}
