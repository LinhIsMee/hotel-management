package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ROOM_TYPES")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    @NotNull(message = "Room type name cannot be null")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "base_price", nullable = false)
    @NotNull(message = "Room type base price cannot be null")
    private Double basePrice;

    @Column(name = "capacity", nullable = false)
    @Min(value = 1, message = "Room type capacity must be greater than 0")
    private int capacity;
}
