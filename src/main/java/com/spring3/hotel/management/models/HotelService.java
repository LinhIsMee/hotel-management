package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "services")
public class HotelService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @NotNull(message = "Tên dịch vụ không được để trống")
    private String name;

    @Column(nullable = false)
    @NotNull(message = "Giá dịch vụ không được để trống")
    private Double price;
} 