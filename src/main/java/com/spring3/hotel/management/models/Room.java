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
    
    @Column
    private String description;
    
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "room_images", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
    
    @Transient
    @Builder.Default
    private List<Service> services = new ArrayList<>();
    
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
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}
