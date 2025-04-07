package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "transaction_no", unique = true)
    private String transactionNo;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "order_info")
    private String orderInfo;

    @Column(name = "status")
    private String status;

    @Column(name = "response_code")
    private String responseCode;

    @Column(name = "bank_code")
    private String bankCode;

    @Column(name = "pay_date")
    private String payDate;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "transaction_status")
    private String transactionStatus;

    @Column(name = "tmn_code")
    private String tmnCode;

    @Column(name = "method")
    private String method;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "booking_id", insertable = false, updatable = false)
    private Integer bookingId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
