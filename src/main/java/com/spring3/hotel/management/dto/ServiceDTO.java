package com.spring3.hotel.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private Boolean isActive;
} 