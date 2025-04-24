package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotNull(message = "Tên đăng nhập không được để trống")
    private String username;

    @Column(nullable = false)
    @NotNull(message = "Mật khẩu không được để trống")
    private String password;

    @Column(unique = true)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    @NotNull(message = "Vai trò không được để trống")
    private String role; // CUSTOMER, STAFF, ADMIN
} 
