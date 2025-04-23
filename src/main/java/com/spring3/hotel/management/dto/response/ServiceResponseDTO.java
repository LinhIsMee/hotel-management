package com.spring3.hotel.management.dto.response;

import com.spring3.hotel.management.models.HotelService;
import com.spring3.hotel.management.models.Offering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
    private BigDecimal price;
    private Boolean isAvailable;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal totalPrice;
    
    public static ServiceResponseDTO fromEntity(HotelService service) {
        if (service == null) return null;
        
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .code(service.getCode())
                .type(service.getType() != null ? service.getType().name() : null)
                .description(service.getDescription())
                .price(service.getPrice())
                .isAvailable(service.getIsAvailable())
                .imageUrl(service.getImageUrl())
                .build();
    }
    
    public static ServiceResponseDTO fromOffering(Offering offering) {
        if (offering == null) return null;
        
        return ServiceResponseDTO.builder()
                .id(offering.getId())
                .name(offering.getName())
                .description(offering.getDescription())
                .price(offering.getPrice() != null ? BigDecimal.valueOf(offering.getPrice()) : null)
                .build();
    }
} 