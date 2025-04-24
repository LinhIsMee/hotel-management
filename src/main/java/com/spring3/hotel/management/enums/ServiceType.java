package com.spring3.hotel.management.enums;

/**
 * Enum cho các loại dịch vụ khách sạn
 */
public enum ServiceType {
    FOOD("Dịch vụ ăn uống"),
    LAUNDRY("Dịch vụ giặt ủi"),
    SPA("Dịch vụ spa"),
    TRANSPORT("Dịch vụ đưa đón"),
    ENTERTAINMENT("Dịch vụ giải trí"),
    CLEANING("Dịch vụ dọn phòng"),
    HOUSEKEEPING("Dịch vụ vệ sinh phòng"),
    TOUR("Dịch vụ du lịch"),
    BUSINESS("Dịch vụ kinh doanh"),
    OTHERS("Dịch vụ khác");
    
    private final String description;
    
    ServiceType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 