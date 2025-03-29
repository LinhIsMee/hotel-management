package com.spring3.hotel.management.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyRatingRequest {
    
    @NotBlank(message = "Nội dung phản hồi không được để trống")
    private String replyComment;
    
    @NotBlank(message = "Người phản hồi không được để trống")
    private String replyBy;
} 