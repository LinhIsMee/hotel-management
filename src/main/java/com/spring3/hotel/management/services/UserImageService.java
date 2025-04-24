package com.spring3.hotel.management.services;

import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.models.UserImage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserImageService {
    /**
     * Lấy danh sách ảnh của người dùng
     */
    List<UserImage> getFilesOfCurrentUser(Integer userId);
    
    /**
     * Upload ảnh mới
     */
    UserImage uploadFile(MultipartFile file, Integer userId) throws IOException;
    
    /**
     * Lấy ảnh theo ID
     */
    UserImage getFileById(Integer id);
    
    /**
     * Xóa ảnh theo ID
     */
    void deleteFile(Integer id);
    
    /**
     * Lưu ảnh vào database
     */
    UserImage saveUserImage(MultipartFile file, User user) throws IOException;
    
    /**
     * Lấy ảnh theo id (trả về Optional)
     */
    Optional<UserImage> getUserImageById(Integer id);
    
    /**
     * Lấy ảnh theo filename
     */
    Optional<UserImage> getUserImageByFilename(String filename);
    
    /**
     * Lấy tất cả ảnh của một user
     */
    List<UserImage> getUserImagesByUserId(Integer userId);
}
