package com.spring3.hotel.management.exceptions;

public class DiscountNotFoundException extends RuntimeException {
    public DiscountNotFoundException(String message) {
        super(message);
    }
    
    public DiscountNotFoundException(Integer id) {
        super("Không tìm thấy mã giảm giá với ID: " + id);
    }
    
    public DiscountNotFoundException(String code, String field) {
        super("Không tìm thấy mã giảm giá với " + field + ": " + code);
    }
} 