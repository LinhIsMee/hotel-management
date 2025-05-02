package com.spring3.hotel.management.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    
    @NotNull(message = "ID người dùng không được để trống")
    private Integer userId;
    
    @NotNull(message = "ID đặt phòng không được để trống")
    private String bookingId;
    
    @NotNull(message = "Đánh giá tổng thể không được để trống")
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    private Integer rating;
    
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    private Integer cleanliness;
    
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    private Integer service;
    
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    private Integer comfort;
    
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    private Integer location;
    
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    private Integer facilities;
    
    @Min(value = 1, message = "Đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Đánh giá tối đa là 5")
    private Integer valueForMoney;
    
    private String comment;
    
    private Boolean isAnonymous = false;
} 