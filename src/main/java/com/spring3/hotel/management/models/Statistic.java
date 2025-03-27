package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "STATISTICS")
public class Statistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "total_revenue", nullable = false)
    private Double totalRevenue;

    @Column(name = "total_bookings", nullable = false)
    private Integer totalBookings;

    @Column(name = "total_customers", nullable = false)
    private Integer totalCustomers;

    @Column(name = "total_rates", nullable = false)
    private Integer totalRates;
}
