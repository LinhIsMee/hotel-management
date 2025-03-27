package com.spring3.hotel.management.repositories;

import com.spring3.hotel.management.models.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserImageRepository extends JpaRepository<UserImage, Integer> {
    List<UserImage> findByUser_IdOrderByCreatedAtDesc(Integer userId);
}
