package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EMPLOYEES")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "EMAIL", unique = true, nullable = false)
    private String email;

    @Column(name = "PHONE", unique = true)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "DEPARTMENT", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "POSITION", nullable = false)
    private Position position;

    @Column(name = "JOIN_DATE", nullable = false)
    private LocalDate joinDate;

    @Column(name = "STATUS", nullable = false)
    private Boolean status = true; // Mặc định là active
} 