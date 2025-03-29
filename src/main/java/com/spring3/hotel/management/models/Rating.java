package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String bookingId;
    
    @Column(nullable = false)
    private String guestName;
    
    @Column(nullable = false)
    private String roomNumber;
    
    @Column(nullable = false)
    private String roomType;
    
    @Column(nullable = false)
    private Integer rating;
    
    private Integer cleanliness;
    
    private Integer service;
    
    private Integer comfort;
    
    private Integer location;
    
    private Integer facilities;
    
    private Integer valueForMoney;
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    @ElementCollection
    @CollectionTable(name = "rating_images", joinColumns = @JoinColumn(name = "rating_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String replyComment;
    
    private String replyBy;
    
    private LocalDateTime replyDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingStatus status;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum RatingStatus {
        PENDING, REPLIED
    }
    
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = RatingStatus.PENDING;
        }
    }
} 