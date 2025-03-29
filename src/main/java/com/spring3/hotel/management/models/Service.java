package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "services")
public class Service {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String code;
    
    @Column(nullable = false)
    private String type; // FOOD, LAUNDRY, SPA, TRANSPORT, ENTERTAINMENT, etc.
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    @Column(nullable = false)
    private String unit; // PER_PERSON, PER_KG, PER_USE, PER_DAY, etc.
    
    @Column
    private Boolean isAvailable;
    
    @Column
    private LocalDate createdAt;
    
    @Column
    private LocalDate updatedAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        if(this.isAvailable == null) {
            this.isAvailable = true;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }
} 