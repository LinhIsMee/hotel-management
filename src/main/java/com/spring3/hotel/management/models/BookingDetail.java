package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "booking_details")
public class BookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    @NotNull(message = "Đặt phòng không được để trống")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @NotNull(message = "Phòng không được để trống")
    private Room room;

    @Column(name = "price_per_night")
    @NotNull(message = "Giá theo đêm không được để trống")
    private Double pricePerNight;

    @Column
    private Integer adults;

    @Column
    private Integer children;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private HotelService service; // Dịch vụ tuỳ chọn
}
