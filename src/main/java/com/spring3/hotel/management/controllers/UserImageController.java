package com.spring3.hotel.management.controllers;

import com.spring3.hotel.management.dto.response.ApiResponse;
import com.spring3.hotel.management.models.User;
import com.spring3.hotel.management.models.UserImage;
import com.spring3.hotel.management.services.UserImageService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class UserImageController {
    private final UserImageService imageService;

    /**
     * Lấy danh sách hình ảnh của người dùng
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<UserImage>>> getFilesOfCurrentUser(@RequestParam Integer userId) {
        List<UserImage> images = imageService.getFilesOfCurrentUser(userId);
        return ResponseEntity.ok(ApiResponse.<List<UserImage>>builder()
                .success(true)
                .message("Lấy danh sách ảnh thành công")
                .data(images)
                .build());
    }

    /**
     * Tải lên hình ảnh mới cho người dùng
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<UserImage>> uploadFile(
            @RequestParam("file") MultipartFile file, 
            @RequestParam Integer userId) throws IOException {
        
        UserImage savedImage = imageService.uploadFile(file, userId);
        
        return ResponseEntity.ok(ApiResponse.<UserImage>builder()
                .success(true)
                .message("Upload ảnh thành công")
                .data(savedImage)
                .build());
    }

    /**
     * Xem hình ảnh theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImageById(@PathVariable Integer id) {
        UserImage image = imageService.getFileById(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getType()));
        
        return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
    }
    
    /**
     * Lấy ảnh theo filename
     */
    @GetMapping("/file/{filename}")
    public ResponseEntity<byte[]> getImageByFilename(@PathVariable String filename) {
        UserImage image = imageService.getUserImageByFilename(filename)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.getType()));
        
        return new ResponseEntity<>(image.getImageData(), headers, HttpStatus.OK);
    }

    /**
     * Xóa hình ảnh theo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable Integer id) {
        imageService.deleteFile(id);
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa ảnh thành công")
                .build());
    }
}