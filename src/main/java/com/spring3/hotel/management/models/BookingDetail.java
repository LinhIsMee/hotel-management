package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "BOOKING_DETAILS")
public class BookingDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "price_per_night")
    private Double pricePerNight;
    
    @Column(name = "room_number")
    private String roomNumber;
    
    @Column(name = "room_type")
    private Integer roomType;
    
    @Column(name = "price")
    private Double price;
    
    @Column(name = "adults")
    private Integer adults;
    
    @Column(name = "children")
    private Integer children;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "booking_detail_services",
        joinColumns = @JoinColumn(name = "booking_detail_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<HotelService> services = new ArrayList<>();
    
    @OneToMany(mappedBy = "detail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingService> bookingServices = new ArrayList<>();
    
    @Transient
    public String getRoomNumber() {
        return room != null ? room.getRoomNumber() : roomNumber;
    }
    
    @Transient
    public String getRoomType() {
        return room != null && room.getRoomType() != null ? 
               room.getRoomType().getName() : (roomType != null ? roomType.toString() : "Unknown");
    }
    
    public void setServices(List<HotelService> services) {
        this.services = services != null ? services : new ArrayList<>();
    }
}
