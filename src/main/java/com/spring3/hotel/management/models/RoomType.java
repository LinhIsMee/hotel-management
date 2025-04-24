package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "room_types")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    @NotNull(message = "Tên loại phòng không được để trống")
    private String name;

    @Column
    private String description;

    @Column(name = "price_per_night", nullable = false)
    @NotNull(message = "Giá theo đêm không được để trống")
    private Double pricePerNight;

    @Column(nullable = false)
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;
}
