package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, unique = true)
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
    @CollectionTable(name = "review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String replyComment;
    
    private String replyBy;
    
    private LocalDateTime replyDate;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;
    
    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;
    
    public enum ReviewStatus {
        PENDING, REPLIED, HIDDEN
    }
    
    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = ReviewStatus.PENDING;
        }
        if (isFeatured == null) {
            isFeatured = false;
        }
        if (isAnonymous == null) {
            isAnonymous = false;
        }
    }
    
    // Phương thức tiện ích để hiển thị tên khách hàng
    public String getDisplayName() {
        if (isAnonymous) {
            // Ẩn danh chỉ hiển thị họ và ký tự đầu tiên của tên
            String[] nameParts = guestName.split(" ");
            if (nameParts.length > 1) {
                return nameParts[0] + " " + nameParts[nameParts.length - 1].charAt(0) + ".";
            } else {
                return guestName.charAt(0) + "***";
            }
        }
        return guestName;
    }
}
