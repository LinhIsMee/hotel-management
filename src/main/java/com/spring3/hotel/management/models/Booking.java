package com.spring3.hotel.management.models;

import com.spring3.hotel.management.enums.BookingStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @NotNull(message = "Người dùng không được để trống")
    private User user;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "check_in_date", nullable = false)
    @NotNull(message = "Ngày nhận phòng không được để trống")
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    @NotNull(message = "Ngày trả phòng không được để trống")
    private LocalDate checkOutDate;

    @Column(name = "total_price", nullable = false)
    @NotNull(message = "Tổng giá tiền không được để trống")
    private Double totalPrice;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Trạng thái đặt phòng không được để trống")
    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @Builder.Default
    private List<BookingDetail> bookingDetails = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();
}
