package com.spring3.hotel.management.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_IMAGES")
public class UserImage extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_url")
    private String imageUrl;
    
    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "type")
    private String type; // image/png, image/jpg, ...

    @Column(name = "filename")
    private String filename;

    @Column(name = "alt_text")
    private String altText;
    
    @Column(name = "size")
    private Long size;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
