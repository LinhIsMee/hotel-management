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
@Table(name = "BOOKING_DETAILS")
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "price_per_night")
    private Double pricePerNight;
    
    @Column(name = "room_number")
    private String roomNumber;
    
    @Column(name = "room_type")
    private Integer roomType;
    
    @Column(name = "price")
    private Double price;
    
    @Transient
    public String getRoomNumber() {
        return room != null ? room.getRoomNumber() : roomNumber;
    }
    
    @Transient
    public String getRoomType() {
        return room != null && room.getRoomType() != null ? 
               room.getRoomType().getName() : (roomType != null ? roomType.toString() : "Unknown");
    }
}
