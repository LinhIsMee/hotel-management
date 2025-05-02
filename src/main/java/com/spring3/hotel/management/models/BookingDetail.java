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
    
    @Transient
    public String getRoomNumber() {
        return room != null ? room.getRoomNumber() : null;
    }
    
    @Transient
    public String getRoomType() {
        return room != null && room.getRoomType() != null ? 
               room.getRoomType().getName() : "Unknown";
    }
    
    @Transient
    public Double getPrice() {
        if (booking != null && room != null && room.getRoomType() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                booking.getCheckInDate(), booking.getCheckOutDate());
            if (days < 1) days = 1;
            return room.getRoomType().getBasePrice() * days;
        }
        return pricePerNight; // Fallback
    }
}
