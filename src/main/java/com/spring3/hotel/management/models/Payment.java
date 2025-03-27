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
@Table(name = "PAYMENTS")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "pay_date")
    private String payDate;

    @Column(name = "status", nullable = false)
    private String status;

    private Long amount;

    @Column(name = "transaction_no")
    private String transactionNo;

    @Column(name = "order_info")
    private String orderInfo;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "response_code")
    private String responseCode;
}
