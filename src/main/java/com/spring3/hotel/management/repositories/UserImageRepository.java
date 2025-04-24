package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, Integer> {
    List<UserImage> findByUser_IdOrderByCreatedAtDesc(Integer userId);
    
    Optional<UserImage> findByFilename(String filename);
    
    List<UserImage> findAllByUserId(Integer userId);
}
