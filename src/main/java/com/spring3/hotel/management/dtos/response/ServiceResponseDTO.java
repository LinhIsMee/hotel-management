package com.spring3.hotel.management.dtos.response;

import com.spring3.hotel.management.models.Offering;
import com.spring3.hotel.management.models.Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDTO {
    
    private Integer id;
    private String name;
    private String code;
    private String type;
    private String description;
    private Double price;
    private String unit;
    private Boolean isAvailable;
    private String createdAt;
    private String updatedAt;
    
    // Thêm các trường mới để hỗ trợ BookingService
    private Integer quantity;
    private Double totalPrice;
    
    public static ServiceResponseDTO fromEntity(Service service) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .code(service.getCode())
                .type(service.getType())
                .description(service.getDescription())
                .price(service.getPrice())
                .unit(service.getUnit())
                .isAvailable(service.getIsAvailable())
                .createdAt(service.getCreatedAt() != null ? service.getCreatedAt().format(formatter) : null)
                .updatedAt(service.getUpdatedAt() != null ? service.getUpdatedAt().format(formatter) : null)
                .build();
    }
    
    // Phương thức mới để chuyển đổi từ Offering
    public static ServiceResponseDTO fromOffering(Offering offering) {
        return ServiceResponseDTO.builder()
                .id(offering.getId())
                .name(offering.getName())
                .description(offering.getDescription())
                .price(offering.getPrice())
                .build();
    }
} 