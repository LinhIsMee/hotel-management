package com.spring3.hotel.management.controllers;


import com.spring3.hotel.management.models.UserImage;
import com.spring3.hotel.management.services.impl.UserImageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class UserImageController {
    private final UserImageServiceImpl imageService;

    @GetMapping("/")
    public ResponseEntity<?> getFilesOfCurrentUser(@RequestParam Integer userId) {
        return ResponseEntity.ok(imageService.getFilesOfCurrentUser(userId));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam Integer userId) throws IOException, IOException {
        return ResponseEntity.ok(imageService.uploadFile(file, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> readFile(@PathVariable Integer id) {
        UserImage image = imageService.getFileById(id);
        // Code logic
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getType()))
            .body(image.getData()); // 200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Integer id) {
        imageService.deleteFile(id);
        // Code logic
        return ResponseEntity.noContent().build(); // 204
    }

}