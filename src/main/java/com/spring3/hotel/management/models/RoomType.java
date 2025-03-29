package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ROOM_TYPES")
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    @NotNull(message = "Tên loại phòng không được để trống")
    private String name;
    
    @Column(name = "code", nullable = false, unique = true)
    @NotNull(message = "Mã loại phòng không được để trống")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "base_price", nullable = false)
    @NotNull(message = "Giá cơ bản không được để trống")
    private Double basePrice;
    
    @Column(name = "price_per_night", nullable = false)
    @NotNull(message = "Giá theo đêm không được để trống")
    private Double pricePerNight;

    @Column(name = "capacity", nullable = false)
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    private Integer capacity;
    
    @Column(name = "max_occupancy", nullable = false)
    @Min(value = 1, message = "Số người tối đa phải lớn hơn 0")
    private Integer maxOccupancy;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;
    
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDate.now();
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
        if (this.basePrice == null && this.pricePerNight != null) {
            this.basePrice = this.pricePerNight;
        }
        if (this.pricePerNight == null && this.basePrice != null) {
            this.pricePerNight = this.basePrice;
        }
        if (this.maxOccupancy == null && this.capacity != null) {
            this.maxOccupancy = this.capacity;
        }
        if (this.capacity == null && this.maxOccupancy != null) {
            this.capacity = this.maxOccupancy;
        }
    }
}
