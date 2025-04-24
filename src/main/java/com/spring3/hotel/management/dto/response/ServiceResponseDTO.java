package com.spring3.hotel.management.dto.response;

import com.spring3.hotel.management.models.HotelService;
// import com.spring3.hotel.management.models.Offering; // Commenting out: Missing model
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
    private Double price;
    private Boolean isAvailable;
    private String imageUrl;
    private String unit;
    private Integer quantity;
    private BigDecimal totalPrice;
    
    public static ServiceResponseDTO fromEntity(HotelService service) {
        if (service == null) return null;
        
        return ServiceResponseDTO.builder()
                .id(service.getId())
                .name(service.getName())
                // .code(service.getCode()) // Commenting out: Missing method
                // .type(service.getType()) // Commenting out: Missing method
                // .description(service.getDescription()) // Commenting out: Missing method
                .price(service.getPrice())
                // .isAvailable(service.getIsAvailable()) // Commenting out: Missing method
                // .imageUrl(service.getImageUrl()) // Commenting out: Missing method
                // .unit(service.getUnit()) // Commenting out: Missing method
                .build();
    }
    
    // public static ServiceResponseDTO fromOffering(Offering offering) { // Commenting out: Missing model Offering
    //     if (offering == null) return null;
    //     
    //     return ServiceResponseDTO.builder()
    //             .id(offering.getId())
    //             .name(offering.getName())
    //             .description(offering.getDescription())
    //             .price(offering.getPrice() != null ? BigDecimal.valueOf(offering.getPrice()) : null)
    //             .build();
    // }
} 