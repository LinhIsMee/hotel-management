package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// User Entity
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(nullable = false)
    private String role; // e.g., "CUSTOMER", "STAFF", "ADMIN"
}

// RoomType Entity
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
    private String name;

    @Column
    private String description;

    @Column(name = "price_per_night", nullable = false)
    private Double pricePerNight;

    @Column(nullable = false)
    private Integer capacity;
}

// Room Entity
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
    private String roomNumber;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(nullable = false)
    private String status; // e.g., "VACANT", "OCCUPIED"
}

// Booking Entity
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingDetail> bookingDetails = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<Payment> payments = new ArrayList<>();
}

// BookingStatus Enum
public enum BookingStatus {
    PENDING, CONFIRMED, CANCELLED
}

// BookingDetail Entity
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
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "price_per_night")
    private Double pricePerNight;

    @Column
    private Integer adults;

    @Column
    private Integer children;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private HotelService service; // Optional service
}

// HotelService Entity
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
    private String name;

    @Column(nullable = false)
    private Double price;
}

// Discount Entity
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "discounts")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;
}

// Payment Entity
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(nullable = false)
    private Double amount;

    @Column
    private String method; // e.g., "CASH", "CARD"

    @Column
    private String status; // e.g., "PENDING", "COMPLETED"
}

// Review Entity
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating; // 1-5 stars

    @Column
    private String comment;
}