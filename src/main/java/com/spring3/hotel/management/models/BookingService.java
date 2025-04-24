package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "BOOKING_SERVICES")
public class BookingService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_detail_id")
    private BookingDetail detail;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private HotelService service;

    @Column(name = "quantity")
    private int quantity;
    
    @Column(name = "price")
    private double price;

    @Column(name = "total_price")
    private double totalPrice;
}
