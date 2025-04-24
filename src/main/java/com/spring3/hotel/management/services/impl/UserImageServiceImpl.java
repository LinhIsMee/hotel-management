package com.spring3.hotel.management.services.impl;

import com.spring3.hotel.management.exceptions.NotFoundException;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.models.UserImage;
import com.spring3.hotel.management.repositories.UserImageRepository;
import com.spring3.hotel.management.repositories.UserRepository;
import com.spring3.hotel.management.services.UserImageService;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserImageServiceImpl implements UserImageService {
    private final UserImageRepository userImageRepository;
    private final UserRepository userRepository;

    @Override
    public List<UserImage> getFilesOfCurrentUser(Integer userId) {
        return userImageRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public UserImage uploadFile(MultipartFile file, Integer userId) throws IOException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id " + userId));
        
        return saveUserImage(file, user);
    }

    @Override
    public UserImage getFileById(Integer id) {
        return userImageRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("File not found with id " + id));
    }

    @Override
    public void deleteFile(Integer id) {
        UserImage file = getFileById(id);
        userImageRepository.delete(file);
    }
    
    @Override
    public UserImage saveUserImage(MultipartFile file, User user) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        
        // Tạo URL để truy cập ảnh qua API
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/images/file/")
                .path(fileName)
                .toUriString();
                
        UserImage userImage = UserImage.builder()
                .imageData(file.getBytes())
                .imageUrl(imageUrl)
                .filename(fileName)
                .type(file.getContentType())
                .size(file.getSize())
                .user(user)
                .build();
                
        return userImageRepository.save(userImage);
    }
    
    @Override
    public Optional<UserImage> getUserImageById(Integer id) {
        return userImageRepository.findById(id);
    }
    
    @Override
    public Optional<UserImage> getUserImageByFilename(String filename) {
        return userImageRepository.findByFilename(filename);
    }
    
    @Override
    public List<UserImage> getUserImagesByUserId(Integer userId) {
        return userImageRepository.findAllByUserId(userId);
    }
}
