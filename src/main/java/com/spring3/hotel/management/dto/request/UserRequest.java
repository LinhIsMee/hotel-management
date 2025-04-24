package com.spring3.hotel.management.dto.request;

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
    private String role;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;

}
