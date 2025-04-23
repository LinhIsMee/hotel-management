package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    
    @Column(name = "status", nullable = false)
    private String status; // VACANT, OCCUPIED, MAINTENANCE, CLEANING
    
    @Column
    private String floor;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column
    private String notes;
    
    @Column(name = "average_rating")
    private Double averageRating = 0.0;
    
    @Column(name = "rating_count")
    private Integer ratingCount = 0;
    
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "room_services",
        joinColumns = @JoinColumn(name = "room_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<HotelService> services = new ArrayList<>();
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Rating> ratings = new ArrayList<>();
    
    @Column
    private LocalDate createdAt;
    
    @Column
    private LocalDate updatedAt;
    
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDate.now();
        if(this.status == null) {
            this.status = "VACANT";
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }

    public void updateAverageRating(Integer newRating) {
        if (this.averageRating == null) this.averageRating = 0.0;
        if (this.ratingCount == null) this.ratingCount = 0;
        
        double total = this.averageRating * this.ratingCount + newRating;
        this.ratingCount++;
        this.averageRating = total / this.ratingCount;
    }
    
    // Tính lại điểm trung bình từ tất cả đánh giá
    public void updateAverageRating() {
        if (ratings == null || ratings.isEmpty()) {
            this.averageRating = 0.0;
            this.ratingCount = 0;
            return;
        }
        
        double total = 0;
        for (Rating rating : ratings) {
            if (rating.getStars() != null) {
                total += rating.getStars();
            }
        }
        
        this.ratingCount = ratings.size();
        this.averageRating = total / this.ratingCount;
    }
}
