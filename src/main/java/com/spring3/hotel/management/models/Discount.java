package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

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
    @NotNull(message = "Mã giảm giá không được để trống")
    private String code;

    @Column(name = "discount_type", nullable = false)
    @NotNull(message = "Loại giảm giá không được để trống")
    private String discountType;

    @Column(name = "discount_value", nullable = false)
    @NotNull(message = "Giá trị giảm giá không được để trống")
    private Double discountValue;

    @Column(name = "valid_from", nullable = false)
    @NotNull(message = "Ngày bắt đầu hiệu lực không được để trống")
    private LocalDate validFrom;

    @Column(name = "valid_to", nullable = false)
    @NotNull(message = "Ngày kết thúc hiệu lực không được để trống")
    private LocalDate validTo;
    
    @Column(name = "max_uses", nullable = false)
    private int maxUses = 1;
    
    @Column(name = "used_count", nullable = false)
    private int usedCount = 0;
}
