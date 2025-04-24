package com.spring3.hotel.management.models;

import com.spring3.hotel.management.enums.RoomStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "room_number", nullable = false, unique = true)
    @NotNull(message = "Số phòng không được để trống")
    private String roomNumber;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    @NotNull(message = "Loại phòng không được để trống")
    private RoomType roomType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Trạng thái phòng không được để trống")
    private RoomStatus status;
}
