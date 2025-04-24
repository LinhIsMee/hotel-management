package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ratings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Số sao đánh giá (1-5)
    @Column(nullable = false)
    private Integer stars;

    // Bình luận của khách hàng
    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        if (room != null) {
            room.updateAverageRating(this.stars);
        }
    }
} 