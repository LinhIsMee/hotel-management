package com.spring3.hotel.management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USERS")
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;
    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    private String address;

    @Column(name = "national_id", unique = true)
    private String nationalId;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;
} 
