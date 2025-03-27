package com.spring3.hotel.management.dtos.response;

import com.spring3.hotel.management.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserResponse {

    private Integer id;
    private String username;
    private Role role;


}
