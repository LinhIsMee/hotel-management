package com.spring3.hotel.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertRoomTypeRequest {
    
    @NotBlank(message = "Tên loại phòng không được để trống")
    @Size(max = 100, message = "Tên loại phòng không được vượt quá 100 ký tự")
    private String name;
    
    @NotBlank(message = "Mã loại phòng không được để trống")
    @Size(max = 20, message = "Mã loại phòng không được vượt quá 20 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]*$", message = "Mã loại phòng chỉ được chứa chữ hoa, số và dấu gạch dưới")
    private String code;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
    
    @NotNull(message = "Giá theo đêm không được để trống")
    @Min(value = 0, message = "Giá theo đêm phải lớn hơn hoặc bằng 0")
    private Double pricePerNight;
    
    @NotNull(message = "Số người tối đa không được để trống")
    @Min(value = 1, message = "Số người tối đa phải lớn hơn 0")
    @Max(value = 10, message = "Số người tối đa không được vượt quá 10")
    private Integer maxOccupancy;
    
    @NotEmpty(message = "Danh sách tiện nghi không được để trống")
    private List<String> amenities;
    
    @URL(message = "URL hình ảnh không hợp lệ")
    private String imageUrl;
    
    @NotNull(message = "Trạng thái hoạt động không được để trống")
    private Boolean isActive;
} 